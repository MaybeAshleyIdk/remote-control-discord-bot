package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason

public sealed class Request {

	public abstract val handle: RequestHandle

	public data class Status(override val handle: RequestHandle.Status) : Request()

	public data class Pause(
		val reason: BotPauseReason?,
		override val handle: RequestHandle.Pause,
	) : Request()

	public data class Resume(override val handle: RequestHandle.Resume) : Request()

	public data class Shutdown(override val handle: RequestHandle.Shutdown) : Request()
}
