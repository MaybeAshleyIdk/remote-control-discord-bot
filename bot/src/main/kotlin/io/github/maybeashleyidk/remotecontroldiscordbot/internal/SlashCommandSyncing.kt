package io.github.maybeashleyidk.remotecontroldiscordbot.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils.await
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandDetails
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandName
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.IntegrationType
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.interactions.commands.Command
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData

@JvmInline
private value class SlashCommandName(val nameString: String)

@JvmInline
private value class SlashCommandId(val idLong: Long)

internal suspend fun Jda.syncSlashCommandsWithLocalCommands(
	localCommands: ImmutableMap<LocalCommandName, LocalCommandDetails>,
	logger: Logger,
): ImmutableMap<Long, LocalCommand> {
	return coroutineScope {
		val deferredExistingSlashCommands: Deferred<List<Command>> =
			async {
				this@syncSlashCommandsWithLocalCommands.retrieveCommands(/* withLocalizations = */ true).await()
					.filter { command: Command ->
						command.type == Command.Type.SLASH
					}
			}

		val incomingCommands: Map<SlashCommandName, Pair<SlashCommandData, LocalCommand>> = localCommands.entries
			.associate { (localCommandName: LocalCommandName, localCommandDetails: LocalCommandDetails) ->
				val localCommand = LocalCommand(localCommandName, localCommandDetails)

				SlashCommandName(localCommandName.toString()) to (localCommand.toSlashCommandData() to localCommand)
			}

		val existingSlashCommands: List<Command> = deferredExistingSlashCommands.await()
		val existingSlashCommandNames: Set<SlashCommandName> = existingSlashCommands
			.mapTo(destination = HashSet(existingSlashCommands.size)) { existingCommand: Command ->
				SlashCommandName(existingCommand.name)
			}

		val createdCommands: Deferred<Map<SlashCommandId, LocalCommand>> =
			async {
				this@syncSlashCommandsWithLocalCommands
					.createSlashCommands(incomingCommands, existingSlashCommandNames, logger)
			}

		val editedCommands: Deferred<Map<SlashCommandId, LocalCommand>> =
			async {
				this@syncSlashCommandsWithLocalCommands
					.editAndDeleteSlashCommands(incomingCommands, existingSlashCommands, logger)
			}

		(createdCommands.await() + editedCommands.await())
			.mapKeys { (slashCommandId: SlashCommandId, _: LocalCommand) ->
				slashCommandId.idLong
			}
			.toImmutableMap()
	}
}

private suspend fun Jda.createSlashCommands(
	incomingCommands: Map<SlashCommandName, Pair<SlashCommandData, LocalCommand>>,
	existingSlashCommandNames: Set<SlashCommandName>,
	logger: Logger,
): Map<SlashCommandId, LocalCommand> {
	val actions: List<suspend () -> Pair<SlashCommandId, LocalCommand>> = incomingCommands
		.filter { (name: SlashCommandName, _: Pair<SlashCommandData, LocalCommand>) ->
			name !in existingSlashCommandNames
		}
		.values
		.map { (slashCommandData: SlashCommandData, localCommand: LocalCommand) ->
			suspend {
				val createdSlashCommand: Command = this@createSlashCommands
					.upsertCommand(slashCommandData)
					.await()

				logger.logInfo(
					"""
					Created the following slash command:
						Name: /${createdSlashCommand.fullCommandName}
						ID: ${createdSlashCommand.id}
						Command line: ${localCommand.argv.toCommandLine()}
					""".trimIndent(),
				)

				SlashCommandId(createdSlashCommand.idLong) to localCommand
			}
		}

	return coroutineScope {
		actions
			.map { action: suspend () -> Pair<SlashCommandId, LocalCommand> ->
				async { action() }
			}
			.associateTo(
				destination = LinkedHashMap(actions.size),
			) { deferredEntry: Deferred<Pair<SlashCommandId, LocalCommand>> ->
				deferredEntry.await()
			}
	}
}

private suspend fun Jda.editAndDeleteSlashCommands(
	incomingCommands: Map<SlashCommandName, Pair<SlashCommandData, LocalCommand>>,
	existingSlashCommands: List<Command>,
	logger: Logger,
): Map<SlashCommandId, LocalCommand> {
	val actions: List<suspend () -> Pair<SlashCommandId, LocalCommand>?> = existingSlashCommands
		.map { existingSlashCommand: Command ->
			val existingSlashCommandName = SlashCommandName(existingSlashCommand.name)
			val incomingCommand: Pair<SlashCommandData, LocalCommand>? = incomingCommands[existingSlashCommandName]

			if (incomingCommand == null) {
				return@map suspend {
					this.deleteCommandById(existingSlashCommand.idLong).await()

					logger.logInfo("Deleted the slash command ${existingSlashCommand.toLogString()}")

					null
				}
			}

			val (incomingSlashCommandData: SlashCommandData, incomingLocalCommand: LocalCommand) = incomingCommand

			if (existingSlashCommand.isDataEqualTo(incomingSlashCommandData)) {
				return@map { SlashCommandId(existingSlashCommand.idLong) to incomingLocalCommand }
			}

			suspend {
				val editedCommand: Command = this@editAndDeleteSlashCommands
					.editCommandById(Command.Type.SLASH, existingSlashCommand.idLong)
					.apply(incomingSlashCommandData)
					.await()

				logger.logInfo(
					"""
					Edited the slash command ${editedCommand.toLogString()}:
						New command line: ${incomingLocalCommand.argv.toCommandLine()}
					""".trimIndent(),
				)

				SlashCommandId(editedCommand.idLong) to incomingLocalCommand
			}
		}

	return coroutineScope {
		actions
			.map { action: suspend () -> Pair<SlashCommandId, LocalCommand>? ->
				async { action() }
			}
			.awaitAll()
			.filterNotNull()
			.associateTo(LinkedHashMap(actions.size)) { it }
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

private fun Command.isDataEqualTo(data: SlashCommandData): Boolean {
	return SlashCommandData.fromCommand(this).toData() == data.toData()
}

private fun Command.toLogString(): String {
	return "/${this.fullCommandName} (ID: ${this.id})"
}
