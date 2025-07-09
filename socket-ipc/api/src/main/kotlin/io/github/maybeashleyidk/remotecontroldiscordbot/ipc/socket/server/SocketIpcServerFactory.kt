package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

public interface SocketIpcServerFactory {

	public sealed class BindResult {

		public data class Success(public val server: SocketIpcServer) : BindResult()

		public data object SocketAlreadyExists : BindResult()
	}

	public fun bind(): BindResult
}
