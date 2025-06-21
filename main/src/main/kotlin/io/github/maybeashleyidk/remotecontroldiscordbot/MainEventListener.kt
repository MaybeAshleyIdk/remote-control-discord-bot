package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.UiStringResolver.WithLocale.Companion.resolve
import io.github.maybeashleyidk.remotecontroldiscordbot.UiStringResolver.WithLocale.Companion.withLocale
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logWarning
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.withScope
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.NestedSupervisorJob
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.await
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.exec
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.isUtf8SafePrintable
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.plus
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.shutDownAndAwaitTermination
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.useSecureTemporaryFile
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.future.asDeferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import kotlinx.coroutines.supervisorScope
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.GenericEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.session.ShutdownEvent
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.requests.CloseCode
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import java.io.Closeable
import java.nio.ByteBuffer
import java.nio.channels.Channels
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext

internal class MainEventListener(
	parentCoroutineScope: CoroutineScope,
	private val deferredConfig: Deferred<Config>,
	private val uiStringResolver: UiStringResolver,
	logger: Logger,
) : EventListener, Closeable {

	data class Config(
		val authorizedUserIds: ImmutableSet<Long>,
		val commandsMap: ImmutableMap<Long, LocalCommand>,
	)

	private val logger: Logger = logger.withScope("MainEventListener")

	private var closing: AtomicBoolean = AtomicBoolean(false)

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

	private val eventHandlingCoroutineScope: CoroutineScope = parentCoroutineScope +
		Dispatchers.Default +
		NestedSupervisorJob()

	init {
		deferredConfig.invokeOnCompletion { cause: Throwable? ->
			if (cause != null) {
				return@invokeOnCompletion
			}

			this@MainEventListener.logger.logInfo("Ready to receive slash command interaction events")
		}
	}

	override fun onEvent(event: GenericEvent) {
		if (this.closing.get()) {
			return
		}

		when (event) {
			is ShutdownEvent -> {
				val codeStr: String = event.code
					.let { code: Int ->
						CloseCode.from(code)
							?.let { closeCode: CloseCode ->
								"${closeCode.name} (code: ${closeCode.code}): \"${closeCode.meaning}\""
							}
							?: "code=$code"
					}
				this.logger.logInfo("Received shutdown event: $codeStr")

				this.close()
			}

			is SlashCommandInteractionEvent -> {
				this.eventHandlingCoroutineScope.launch {
					if (this@MainEventListener.closing.get()) {
						return@launch
					}

					this@MainEventListener.handleSlashCommandInteraction(event)
				}
			}
		}
	}

	private suspend fun handleSlashCommandInteraction(event: SlashCommandInteractionEvent) {
		this.logger.logInfo(
			"Received slash command interaction event:\n" +
				"/${event.fullCommandName} (${event.commandId})\n" +
				"By user ${event.user.toLogString()}",
		)

		val config: Config = this.deferredConfig.await()

		val uiStringResolver: UiStringResolver.WithLocale = this.uiStringResolver.withLocale(event.userLocale)

		if (event.user.idLong !in config.authorizedUserIds) {
			event.reply(uiStringResolver.resolve(UiStringKey.UnauthorizedUser))
				.setEphemeral(true)
				.await()

			this.logger.logInfo("The unauthorized user ${event.user.toLogString()} tried to use a slash command")

			return
		}

		val localCommand: LocalCommand? = config.commandsMap[event.interaction.commandIdLong]

		if (localCommand == null) {
			val message: String = "The slash command with the ID ${event.commandId} (/${event.fullCommandName}) " +
				"is not known. Bailing out"
			this.logger.logWarning(message)

			return
		}

		val deferredInteractionHook: Deferred<InteractionHook> = event.deferReply(/* ephemeral = */ true)
			.submit()
			.asDeferred()

		val basePrefix = "remote-control-discord-bot_command_${localCommand.name}"
		useSecureTemporaryFile(prefix = "${basePrefix}_stdout") { stdoutFile: FileChannel ->
			useSecureTemporaryFileIf(
				condition = !(localCommand.isStderrIgnored),
				prefix = "${basePrefix}_stderr",
			) { stderrFile: FileChannel? ->
				val messageCreateData: MessageCreateData =
					this.executeCommand(
						localCommand = localCommand,
						stdoutFile = stdoutFile,
						stderrFile = stderrFile,
						uiStringResolver = uiStringResolver,
					)

				deferredInteractionHook.await().sendMessage(messageCreateData).await()
			}
		}
	}

	private suspend fun executeCommand(
		localCommand: LocalCommand,
		stdoutFile: FileChannel,
		stderrFile: FileChannel?,
		uiStringResolver: UiStringResolver.WithLocale,
	): MessageCreateData {
		this.logger.logInfo("Executing the local command ${localCommand.toLogString()}")

		val exitStatusCode: Int = supervisorScope {
			withContext(commandExecutionCoroutineContext) {
				exec(
					argv = localCommand.argv,
					stdout = Channels.newOutputStream(stdoutFile),
					stderr = stderrFile?.let(Channels::newOutputStream),
				)
			}
		}

		if (exitStatusCode == 0) {
			this.logger.logInfo("Successfully executed the local command ${localCommand.toLogString()}")
		} else {
			val message = "The local command ${localCommand.toLogString()} exited with the status code $exitStatusCode"
			this.logger.logWarning(message)
		}

		val message: String =
			if (exitStatusCode == 0) {
				uiStringResolver.resolve(UiStringKey.LocalCommandExecution.Success)
			} else {
				uiStringResolver.resolve(UiStringKey.LocalCommandExecution.Failure, exitStatusCode)
			}

		val (stdoutFileUpload: FileUpload?, stderrFileUpload: FileUpload?) = withContext(Dispatchers.IO) {
			val stdoutFileUpload: Deferred<FileUpload?> = async { stdoutFile.toFileUpload(basename = "stdout") }

			val stderrFileUpload: Deferred<FileUpload?>? =
				stderrFile?.let { async { it.toFileUpload(basename = "stderr") } }

			stdoutFileUpload.await() to stderrFileUpload?.await()
		}

		return MessageCreateBuilder()
			.setContent(message)
			.setFiles(listOfNotNull(stdoutFileUpload, stderrFileUpload))
			.build()
	}

	override fun close() {
		val success: Boolean = this.closing.compareAndSet(false, true)
		if (!success) {
			return
		}

		this.logger.logInfo("Closing the event listener")
		this.commandExecutionExecutorService.shutDownAndAwaitTermination()
		this.eventHandlingCoroutineScope.cancel(message = "The event listener was closed")
	}
}

private inline fun <R> useSecureTemporaryFileIf(
	condition: Boolean,
	prefix: String? = null,
	suffix: String? = null,
	block: (channel: FileChannel?) -> R,
): R {
	if (!condition) {
		return block(null)
	}

	return useSecureTemporaryFile(prefix = prefix, suffix = suffix, block)
}

private fun FileChannel.toFileUpload(basename: String): FileUpload? {
	val size: Long = this.size()

	if (size == 0L) {
		return null
	}

	this.position(0)

	if (size >= DEFAULT_BUFFER_SIZE) {
		return FileUpload.fromData(Channels.newInputStream(this), basename)
	}

	val buffer: ByteBuffer = ByteBuffer.allocate(size.toInt() + 1)
	val n: Int = this.read(buffer)

	when {
		(n <= 0) -> return null
		(n > size) -> return FileUpload.fromData(Channels.newInputStream(this), basename)
	}

	val array: ByteArray = buffer.array().copyOf(newSize = n)

	val suffix: String = if (array.isUtf8SafePrintable()) ".txt" else ".bin"

	return FileUpload.fromData(array, "$basename$suffix")
}

private fun User.toLogString(): String {
	return "@${this.name} (${this.id})"
}

private fun LocalCommand.toLogString(): String {
	return "\"${this.name}\" (${this.argv.toCommandLine()})"
}
