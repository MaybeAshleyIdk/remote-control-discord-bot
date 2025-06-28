package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.IpcCommand
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public interface SocketIpcServer : AutoCloseable {

	public suspend fun receiveCommand(): IpcCommand?

	public companion object {

		public fun SocketIpcServer.asFlow(): Flow<IpcCommand> {
			return flow {
				while (true) {
					val command: IpcCommand = this@asFlow.receiveCommand() ?: break
					emit(command)
				}
			}
		}
	}
}
