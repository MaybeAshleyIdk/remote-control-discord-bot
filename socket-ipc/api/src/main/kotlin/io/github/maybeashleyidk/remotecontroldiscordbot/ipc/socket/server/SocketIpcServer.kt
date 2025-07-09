package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

public interface SocketIpcServer : AutoCloseable {

	@FunctionalInterface
	public fun interface RequestHandler {

		public suspend fun handleRequest(request: Request)
	}

	public enum class Result {
		Handled,
		Closed,
	}

	/**
	 * [requestHandler.handleRequest()][RequestHandler.handleRequest] is called in the same context as this function.
	 */
	public suspend fun receiveRequest(requestHandler: RequestHandler): Result

	public companion object {

		public fun SocketIpcServer.asFlow(): Flow<Request> {
			return flow {
				while (true) {
					val result: Result = this@asFlow.receiveRequest(requestHandler = this@flow::emit)

					when (result) {
						Result.Handled -> Unit
						Result.Closed -> break
					}
				}
			}
		}
	}
}
