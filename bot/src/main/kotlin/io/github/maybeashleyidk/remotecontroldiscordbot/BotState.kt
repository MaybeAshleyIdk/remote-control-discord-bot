package io.github.maybeashleyidk.remotecontroldiscordbot

public sealed class BotState {

	public data object Resumed : BotState()

	public data class Paused(val reason: BotPauseReason?) : BotState()
}
