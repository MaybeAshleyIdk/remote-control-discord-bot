package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.internal.MainEventListener
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.UiStringResolver
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.syncSlashCommandsWithLocalCommands
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.performGracefulShutdown
import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.suspendUntilShutdown
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.JDABuilder as JdaBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.hooks.EventListener
import java.util.concurrent.atomic.AtomicBoolean

public fun CoroutineScope.launchBot(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	launch {
		val deferredMainEventListenerConfig: CompletableDeferred<MainEventListener.Config> = CompletableDeferred()

		val jda: Jda =
			startJda(
				token = token,
				deferredMainEventListenerConfig = deferredMainEventListenerConfig,
				logger = logger,
			)

		// CoroutineScope.syncWithJda() cancels the scope that it's called on after JDA has shut down, so it has to be
		// called on the root scope that will cancel everything else.
		// This enables child jobs to wait for cancellation to do cleanup without worry that they might indefinitely
		// keep the parent scope running.
		syncWithJda(jda, logger)

		jda.setUp(localCommandsConfig, deferredMainEventListenerConfig, logger)
	}
}

private fun CoroutineScope.startJda(
	token: BotToken,
	deferredMainEventListenerConfig: CompletableDeferred<MainEventListener.Config>,
	logger: Logger,
): Jda {
	val mainEventListener: EventListener =
		MainEventListener(
			parentCoroutineScope = this,
			deferredConfig = deferredMainEventListenerConfig,
			uiStringResolver = UiStringResolver.English,
			logger = logger,
		)

	return JdaBuilder.createDefault(token.toString())
		.addEventListeners(mainEventListener)
		.setStatus(OnlineStatus.INVISIBLE)
		.build()
}

private fun CoroutineScope.syncWithJda(jda: Jda, logger: Logger) {
	val isJdaShuttingDown = AtomicBoolean(false)
	fun shutDownJdaOnce() {
		val success: Boolean = isJdaShuttingDown.compareAndSet(false, true)
		if (!success) {
			return
		}

		jda.performGracefulShutdown(logger)
	}

	val hasShutdownSequenceStarted = AtomicBoolean(false)
	val shutdownHook =
		Thread {
			hasShutdownSequenceStarted.set(true)

			this@syncWithJda.cancel(message = "The JVM is shutting down")

			shutDownJdaOnce()
		}
	Runtime.getRuntime().addShutdownHook(shutdownHook)

	launch {
		try {
			awaitCancellation()
		} catch (e: CancellationException) {
			val cancellationMessage: String? = e.message?.trim()?.takeIf(String::isNotEmpty)

			val logMessage: String = "The coroutine scope was cancelled " +
				(cancellationMessage?.let { "with the message: \"$it\"" } ?: "without a message")

			logger.logInfo(logMessage)
		} finally {
			shutDownJdaOnce()

			if (!(hasShutdownSequenceStarted.get())) {
				Runtime.getRuntime().removeShutdownHook(shutdownHook)
			}
		}
	}

	launch {
		// This keeps the parent scope from completing while JDA hasn't shut down yet.
		jda.suspendUntilShutdown()
		isJdaShuttingDown.set(true)

		this@syncWithJda.cancel(message = "JDA has shut down")

		if (!(hasShutdownSequenceStarted.get())) {
			Runtime.getRuntime().removeShutdownHook(shutdownHook)
		}
	}
}

private suspend fun Jda.setUp(
	localCommandsConfig: LocalCommandsConfig,
	deferredMainEventListenerConfig: CompletableDeferred<MainEventListener.Config>,
	logger: Logger,
) {
	this.awaitReady()

	logger.logInfo("Syncing slash commands with local commands...")
	val commandsMap: ImmutableMap<Long, LocalCommand> =
		this.syncSlashCommandsWithLocalCommands(localCommands = localCommandsConfig.commandsMap, logger)
	logger.logInfo("Successfully synced slash commands with local commands")

	val mainEventListenerConfig =
		MainEventListener.Config(
			authorizedUserIds = localCommandsConfig.authorizedUserIds,
			commandsMap = commandsMap,
		)
	deferredMainEventListenerConfig.complete(mainEventListenerConfig)

	this.presence.setStatus(OnlineStatus.ONLINE)
}
