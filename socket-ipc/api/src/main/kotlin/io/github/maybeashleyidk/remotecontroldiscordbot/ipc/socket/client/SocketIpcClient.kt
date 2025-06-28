package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.IpcCommand

public interface SocketIpcClient : AutoCloseable {

	public sealed class Response<out T> {

		public data class Acknowledged<out T>(public val data: T) : Response<T>()

		public data object Timeout : Response<Nothing>()

		public data object Unknown : Response<Nothing>()
	}

	public sealed class PauseResponseData {

		public data object Success : PauseResponseData()

		public data object AlreadyPaused : PauseResponseData()
	}

	public sealed class ResumeResponseData {

		public data object Success : PauseResponseData()

		public data object NotPaused : PauseResponseData()
	}

	public suspend fun sendCommand(command: IpcCommand.Ping): Response<Unit>

	public suspend fun sendCommand(command: IpcCommand.Pause): Response<PauseResponseData>
	public suspend fun sendCommand(command: IpcCommand.Resume): Response<ResumeResponseData>

	public suspend fun sendCommand(command: IpcCommand.Shutdown): Response<Unit>
}
