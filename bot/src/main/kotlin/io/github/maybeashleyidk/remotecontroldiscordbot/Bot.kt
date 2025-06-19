package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.performGracefulShutdown
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.JDABuilder as JdaBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.hooks.EventListener

internal fun runBot(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	runBlocking {
		runBotSuspending(token, localCommandsConfig, logger)
	}
}

private suspend fun runBotSuspending(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	coroutineScope {
		val deferredMainEventListenerConfig: CompletableDeferred<MainEventListener.Config> = CompletableDeferred()

		MainEventListener(
			parentCoroutineScope = this@coroutineScope,
			deferredConfig = deferredMainEventListenerConfig,
			uiStringResolver = UiStringResolver.English,
			logger = logger,
		).use { mainEventListener: EventListener ->
			val jda: Jda = JdaBuilder.createDefault(token.toString())
				.addEventListeners(mainEventListener)
				.setStatus(OnlineStatus.INVISIBLE)
				.build()

			Runtime.getRuntime()
				.addShutdownHook(Thread { jda.performGracefulShutdown(logger) })

			jda.awaitReady()

			logger.logInfo("Syncing slash commands with local commands...")
			val commandsMap: ImmutableMap<Long, LocalCommand> =
				jda.syncSlashCommandsWithLocalCommands(localCommands = localCommandsConfig.commandsMap, logger)
			logger.logInfo("Successfully synced slash commands with local commands")

			val mainEventListenerConfig =
				MainEventListener.Config(
					authorizedUserIds = localCommandsConfig.authorizedUserIds,
					commandsMap = commandsMap,
				)
			deferredMainEventListenerConfig.complete(mainEventListenerConfig)

			jda.presence.setStatus(OnlineStatus.ONLINE)

			jda.awaitShutdown()
		}
	}
}
