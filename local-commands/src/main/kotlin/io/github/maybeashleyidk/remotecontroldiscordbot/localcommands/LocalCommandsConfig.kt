package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.toImmutableList

public data class LocalCommandsConfig(
	public val authorizedUserIds: ImmutableSet<Long>,
	public val commandsMap: ImmutableMap<LocalCommandName, LocalCommandDetails>,
) {

	init {
		require(commandsMap.isNotEmpty())
	}

	public fun getCommandsAsList(): ImmutableList<LocalCommand> {
		return this.commandsMap.entries
			.map { (name: LocalCommandName, details: LocalCommandDetails) ->
				LocalCommand(name, details)
			}
			.toImmutableList()
	}
}
