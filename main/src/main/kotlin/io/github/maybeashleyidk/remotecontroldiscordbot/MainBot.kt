package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InvalidToken
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.IpcSocketAlreadyExists
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigDuplicateCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigEmptyCommandLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidCommandName
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigZeroCommandsDefined
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.NonExistingFile
import io.github.maybeashleyidk.remotecontroldiscordbot.env.getInstanceConfigDirectoryPathOrExit
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.Request
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServer.Companion.asFlow
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServerFactory
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.parseToLocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logWarning
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr.StderrLogger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import java.nio.file.attribute.BasicFileAttributeView
import java.nio.file.attribute.FileTime
import kotlin.io.path.div
import kotlin.io.path.fileAttributesView
import kotlin.io.path.readText
import kotlin.time.Duration.Companion.hours

private val LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH: Path = Path.of("commands.cfg")
private val TOKEN_FILE_RELATIVE_PATH: Path = Path.of("token.txt")

internal fun mainBot(processInformation: ProcessInformation, instanceName: InstanceName) {
	val instanceConfigDirectoryPath: Path = getInstanceConfigDirectoryPathOrExit(processInformation, instanceName)
	val ipcSocketPath: Path = getIpcSocketPathOrExit(processInformation, instanceName)

	val localCommandsConfig: LocalCommandsConfig =
		getLocalCommandsConfigOrExit(processInformation, instanceConfigDirectoryPath)

	val token: BotToken = getTokenOrExit(processInformation, instanceConfigDirectoryPath)

	bindSocketIpcServerOrExit(processInformation, instanceName, ipcSocketPath).use { socketIpcServer: SocketIpcServer ->
		runBlocking {
			val deferredBot: CompletableDeferred<Bot> = CompletableDeferred()

			launchSocketIpcServer(socketIpcServer, deferredBot, logger = StderrLogger)

			val bot: Bot = launchBot(token, localCommandsConfig, logger = StderrLogger).await()
			deferredBot.complete(bot)

			launch {
				val ipcSocketAccessTimeSettingJob: Job = launch {
					// <https://specifications.freedesktop.org/basedir-spec/latest/#variables>
					// > To ensure that your files are not removed, they should have their access time timestamp
					// > modified at least once every 6 hours of monotonic time [...]
					while (isActive) {
						delay(2.75.hours)
						ipcSocketPath.setLastAccessTimeToNow()
						delay(2.75.hours)
					}
				}

				bot.join()
				ipcSocketAccessTimeSettingJob.cancel(message = "The bot completed")
			}
		}
	}
}

private fun getLocalCommandsConfigOrExit(
	processInformation: ProcessInformation,
	instanceConfigDirectoryPath: Path,
): LocalCommandsConfig {
	val configFilePath: Path = instanceConfigDirectoryPath / LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH

	val configFileContents: String =
		try {
			configFilePath.readText()
		} catch (_: NoSuchFileException) {
			System.err.println("${processInformation.stderrLoggingTag}: $configFilePath: no such file")
			exitProcess(NonExistingFile)
		}

	val configFileParsingResult: LocalCommandsConfigParsingResult = configFileContents.parseToLocalCommandsConfig()
	return when (configFileParsingResult) {
		is LocalCommandsConfigParsingResult.Success -> configFileParsingResult.config

		is LocalCommandsConfigParsingResult.Failure -> {
			handleLocalCommandsConfigParsingFailure(
				processInformation,
				configFilePath,
				failure = configFileParsingResult,
			)
		}
	}
}

private fun getTokenOrExit(processInformation: ProcessInformation, instanceConfigDirectoryPath: Path): BotToken {
	val tokenFilePath: Path = instanceConfigDirectoryPath / TOKEN_FILE_RELATIVE_PATH

	val tokenFileContents: String =
		try {
			tokenFilePath.readText()
		} catch (_: NoSuchFileException) {
			System.err.println("${processInformation.stderrLoggingTag}: $tokenFilePath: no such file")
			exitProcess(NonExistingFile)
		}

	val token: BotToken? = BotToken.ofString(tokenFileContents.trim())

	if (token == null) {
		System.err.println("${processInformation.stderrLoggingTag}: $tokenFilePath: file contains invalid token")
		exitProcess(InvalidToken)
	}

	return token
}

private fun handleLocalCommandsConfigParsingFailure(
	processInformation: ProcessInformation,
	configFilePath: Path,
	failure: LocalCommandsConfigParsingResult.Failure,
): Nothing {
	val (message: String, exitStatus: ExitStatus) =
		when (failure) {
			is LocalCommandsConfigParsingResult.Failure.InvalidLine -> {
				"${failure.lineNumber}: invalid line" to LocalCommandsConfigInvalidLine
			}

			is LocalCommandsConfigParsingResult.Failure.InvalidCommandName -> {
				val message: String = "${failure.lineNumber}:${failure.columnNumber}: " +
					"${failure.invalidCommandName}: invalid command name"

				message to LocalCommandsConfigInvalidCommandName
			}

			is LocalCommandsConfigParsingResult.Failure.EmptyCommandLine -> {
				val message = "${failure.lineNumber}:${failure.columnNumber}: empty command line"
				message to LocalCommandsConfigEmptyCommandLine
			}

			is LocalCommandsConfigParsingResult.Failure.DuplicateCommand -> {
				val message = "${failure.lineNumber}: ${failure.commandName}: duplicate command"
				message to LocalCommandsConfigDuplicateCommand
			}

			is LocalCommandsConfigParsingResult.Failure.ZeroCommandsDefined -> {
				"zero commands defined" to LocalCommandsConfigZeroCommandsDefined
			}
		}

	System.err.println("${processInformation.stderrLoggingTag}: $configFilePath:$message")
	exitProcess(exitStatus)
}

// region socket IPC

private fun bindSocketIpcServerOrExit(
	processInformation: ProcessInformation,
	instanceName: InstanceName,
	ipcSocketPath: Path,
): SocketIpcServer {
	val serverFactory = SocketIpcServerFactory(ipcSocketPath)
	val result: SocketIpcServerFactory.BindResult = serverFactory.bind()
	return when (result) {
		is SocketIpcServerFactory.BindResult.Success -> result.server

		is SocketIpcServerFactory.BindResult.SocketAlreadyExists -> {
			// TODO
			val indent: String = " ".repeat(processInformation.stderrLoggingTag.length + 2)
			val message: String = "${processInformation.stderrLoggingTag}: the instance '$instanceName' may already " +
				"running.\n" +
				"${indent}If that is not the case, remove the socket file at $ipcSocketPath and try again"
			System.err.println(message)

			exitProcess(IpcSocketAlreadyExists)
		}
	}
}

private fun CoroutineScope.launchSocketIpcServer(server: SocketIpcServer, deferredBot: Deferred<Bot>, logger: Logger) {
	launch {
		runSocketIpcServer(server, deferredBot, logger)
	}
}

private suspend fun runSocketIpcServer(server: SocketIpcServer, deferredBot: Deferred<Bot>, logger: Logger) {
	server.asFlow().collect { request: Request ->
		val bot: Bot = deferredBot.await()

		try {
			handleSocketIpcRequest(bot, request)
		} catch (e: Exception) {
			val exceptionStr: String = "$e\n\t" + e.stackTraceToString().replace("\n", "\n\t")
			logger.logWarning("Failed to handle socket IPC request: $exceptionStr")
		}
	}
}

private suspend fun handleSocketIpcRequest(bot: Bot, request: Request) {
	when (request) {
		is Request.Status -> request.handle.sendCurrentState(currentState = bot.stateReference.load())

		is Request.Pause -> {
			val newState = BotState.Paused(reason = request.reason)
			val oldState: BotState = bot.stateReference.exchange(newState)
			request.handle.sendOldState(oldState)
		}

		is Request.Resume -> {
			val oldState: BotState = bot.stateReference.exchange(newState = BotState.Resumed)
			request.handle.sendOldState(oldState)
		}

		is Request.Shutdown -> {
			bot.cancel(message = "Shutdown requested through socket IPC")
			bot.join()
			request.handle.sendShutdownFinished()
		}
	}
}

// endregion

private fun Path.setLastAccessTimeToNow() {
	this.fileAttributesView<BasicFileAttributeView>()
		.setTimes(
			/* lastModifiedTime = */ null,
			/* lastAccessTime = */ FileTime.fromMillis(System.currentTimeMillis()),
			/* createTime = */ null,
		)
}
