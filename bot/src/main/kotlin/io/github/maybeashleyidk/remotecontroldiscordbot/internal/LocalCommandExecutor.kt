package io.github.maybeashleyidk.remotecontroldiscordbot.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.exec
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.shutDownAndAwaitTermination
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.useSecureTemporaryFile
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logWarning
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.withScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import java.nio.channels.Channels
import java.nio.channels.Channels.newOutputStream
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

internal class LocalCommandExecutor(logger: Logger) : AutoCloseable {

	data class CommandResult(
		val exitStatusCode: Int,
		val stdoutFile: FileChannel,
		val stderrFile: FileChannel?,
	)

	private val logger: Logger = logger.withScope("LocalCommandExecutor")

	private val commandExecutionExecutorService: ExecutorService =
		ThreadPoolExecutor(
			/* corePoolSize = */ 0,
			/* maximumPoolSize = */ 10,
			/* keepAliveTime = */ 60,
			/* unit = */ TimeUnit.SECONDS,
			/* workQueue = */ SynchronousQueue(),
		)

	private val commandExecutionCoroutineContext: CoroutineContext =
		commandExecutionExecutorService.asCoroutineDispatcher()

	suspend fun <R> executeCommand(localCommand: LocalCommand, onExecuted: suspend (CommandResult) -> R): R {
		return useSecureTemporaryFiles(localCommand) { stdoutFile: FileChannel, stderrFile: FileChannel? ->
			val exitStatusCode: Int =
				this.executeCommand(
					localCommand = localCommand,
					stdoutFile = stdoutFile,
					stderrFile = stderrFile,
				)

			val result =
				CommandResult(
					exitStatusCode = exitStatusCode,
					stdoutFile = stdoutFile,
					stderrFile = stderrFile,
				)

			onExecuted(result)
		}
	}

	override fun close() {
		this.logger.logInfo("Closing...")

		this.commandExecutionExecutorService.shutDownAndAwaitTermination()

		this.logger.logInfo("Successfully closed")
	}

	private suspend fun executeCommand(
		localCommand: LocalCommand,
		stdoutFile: FileChannel,
		stderrFile: FileChannel?,
	): Int {
		this.logger.logInfo("Executing the local command ${localCommand.toLogString()}...")

		val exitStatusCode: Int = supervisorScope {
			withContext(this@LocalCommandExecutor.commandExecutionCoroutineContext) {
				exec(
					argv = localCommand.argv,
					stdout = newOutputStream(stdoutFile),
					stderr = stderrFile?.let(Channels::newOutputStream),
				)
			}
		}

		coroutineScope {
			launch {
				withContext(Dispatchers.IO) {
					launch { stdoutFile.position(0) }

					if (stderrFile != null) {
						launch { stderrFile.position(0) }
					}
				}
			}

			launch {
				if (exitStatusCode == 0) {
					logger.logInfo("Successfully executed the local command ${localCommand.toLogString()}")
				} else {
					val message: String = "The local command ${localCommand.toLogString()} exited with " +
						"the status code $exitStatusCode"
					logger.logWarning(message)
				}
			}
		}

		return exitStatusCode
	}
}

private inline fun <R> useSecureTemporaryFiles(
	localCommand: LocalCommand,
	block: (stdoutFile: FileChannel, stderrFile: FileChannel?) -> R,
): R {
	val basePrefix = "remote-control-discord-bot_command_${localCommand.name}"

	return useSecureTemporaryFile(prefix = "${basePrefix}_stdout") { stdoutFile: FileChannel ->
		if (localCommand.isStderrIgnored) {
			block(stdoutFile, null)
		} else {
			useSecureTemporaryFile(prefix = "${basePrefix}_stderr") { stderrFile: FileChannel ->
				block(stdoutFile, stderrFile)
			}
		}
	}
}

private fun LocalCommand.toLogString(): String {
	return "\"${this.name}\" (${this.argv.toCommandLine()})"
}
