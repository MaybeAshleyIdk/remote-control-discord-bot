package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

public interface SocketIpcClientFactory {

	public sealed class ConnectResult {

		public data class Success(public val client: SocketIpcClient) : ConnectResult()

		public data object NoSuchSocket : ConnectResult()
	}

	public fun connect(): ConnectResult
}
