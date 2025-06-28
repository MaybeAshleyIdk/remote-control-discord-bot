package io.github.maybeashleyidk.remotecontroldiscordbot.ipc

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason

public sealed class IpcCommand {

	public data object Ping : IpcCommand()

	public data class Pause(public val reason: BotPauseReason?) : IpcCommand()

	public data object Resume : IpcCommand()

	public data object Shutdown : IpcCommand()
}
