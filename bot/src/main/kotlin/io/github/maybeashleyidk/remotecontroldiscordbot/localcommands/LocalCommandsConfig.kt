package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList

internal data class LocalCommandsConfig(
	val authorizedUserIds: ImmutableSet<Long>,
	val commandsMap: ImmutableMap<LocalCommandName, LocalCommandDetails>,
) {

	init {
		require(commandsMap.isNotEmpty())
	}

	fun getCommandsAsList(): ImmutableList<LocalCommand> {
		return this.commandsMap.entries
			.map { (name: LocalCommandName, details: LocalCommandDetails) ->
				LocalCommand(name, details)
			}
			.toImmutableList()
	}
}
