package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.await
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.performGracefulShutdown
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.JDABuilder as JdaBuilder
import net.dv8tion.jda.api.OnlineStatus
import net.dv8tion.jda.api.hooks.EventListener
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData
import net.dv8tion.jda.api.requests.RestAction

internal fun runBot(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	runBlocking {
		runBotSuspending(token, localCommandsConfig, logger)
	}
}

private suspend fun runBotSuspending(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	coroutineScope {
		val localCommands: List<LocalCommand> = localCommandsConfig.getCommandsAsList()

		val slashCommandDataList: List<SlashCommandData> = localCommands
			.map(LocalCommand::toSlashCommandData)

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

			// TODO: Cache the slash command IDs instead of re-creating them every time.
			logger.logInfo("Deleting all existing slash commands...")
			jda.deleteAllCommands()
			logger.logInfo("Successfully deleted all existing slash commands")

			val commandsMap: ImmutableMap<Long, LocalCommand> = jda.updateCommands()
				.addCommands(slashCommandDataList)
				.also { logger.logInfo("Creating slash commands...") }
				.await()
				.also { logger.logInfo("Successfully created slash commands") }
				.withIndex()
				.associate { (index: Int, command: Command) ->
					command.idLong to localCommands[index]
				}
				.toImmutableMap()
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

private fun LocalCommand.toSlashCommandData(): SlashCommandData {
	val name: String = this.name.toString()

	val commandLine: String = this.argv.toCommandLine()
	val enGbDescription: String =
		createEnGbSlashCommandDescription(uiStringResolver = UiStringResolver.English, commandLine = commandLine)
	val enUsDescription: String =
		createEnUsSlashCommandDescription(uiStringResolver = UiStringResolver.English, commandLine = commandLine)

	return Commands.slash(name, enUsDescription)
		.setIntegrationTypes(IntegrationType.USER_INSTALL)
		.setContexts(InteractionContextType.ALL)
		.setDescriptionLocalization(DiscordLocale.ENGLISH_UK, enGbDescription)
		.setDescriptionLocalization(DiscordLocale.ENGLISH_US, enUsDescription)
}

private suspend fun Jda.deleteAllCommands() {
	val commands: List<Command> = this.retrieveCommands().await()

	if (commands.isEmpty()) {
		return
	}

	commands
		.map { command: Command ->
			this.deleteCommandById(command.idLong)
		}
		.let { RestAction.allOf(it) }
		.await()
}
