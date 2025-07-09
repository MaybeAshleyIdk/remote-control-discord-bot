package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason

public interface SocketIpcClient : AutoCloseable {

	public suspend fun sendStatusRequest(): ResponseHandle.Status

	public suspend fun sendPauseRequest(reason: BotPauseReason?): ResponseHandle.Pause

	public suspend fun sendResumeRequest(): ResponseHandle.Resume

	public suspend fun sendShutdownRequest(): ResponseHandle.Shutdown
}
