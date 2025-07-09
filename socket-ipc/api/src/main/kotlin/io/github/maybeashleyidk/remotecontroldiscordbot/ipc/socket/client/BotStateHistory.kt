package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState

public data class BotStateHistory(
	public val oldState: BotState,
	public val newState: BotState,
)
