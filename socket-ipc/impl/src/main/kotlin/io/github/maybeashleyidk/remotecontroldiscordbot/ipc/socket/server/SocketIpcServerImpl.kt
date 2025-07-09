package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason
import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.BotStateSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.MagicNumberAndRequestTypeDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.OptionalBotPauseReasonDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.RequestType
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.ShutdownFinishedMessageSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServer.RequestHandler
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServer.Result
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult.Companion.getOrElse
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.ReadResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.ReadResult.Companion.getValidOrNull
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.deserialize
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.serialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import java.io.Closeable
import java.nio.channels.AsynchronousCloseException
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel

internal class SocketIpcServerImpl(private val channel: ServerSocketChannel) : SocketIpcServer {

	// TODO: log if a request is dropped and why

	private enum class SinglyTryResult {
		Handled,
		Invalid,
		Closed,
	}

	override suspend fun receiveRequest(requestHandler: RequestHandler): Result {
		while (true) {
			val singlyTryResult: SinglyTryResult = this.receiveRequestSingleTry(requestHandler)

			val result: Result? =
				when (singlyTryResult) {
					SinglyTryResult.Handled -> Result.Handled
					SinglyTryResult.Invalid -> null
					SinglyTryResult.Closed -> Result.Closed
				}

			if (result != null) {
				return result
			}
		}
	}

	private suspend fun receiveRequestSingleTry(requestHandler: RequestHandler): SinglyTryResult {
		return withContext(Dispatchers.IO) {
			try {
				this@SocketIpcServerImpl.channel.accept()
			} catch (_: AsynchronousCloseException) {
				return@withContext null
			}!!
		}.use { connection: SocketChannel? ->
			if (connection == null) {
				return@use SinglyTryResult.Closed
			}

			currentCoroutineContext().ensureActive()

			val executed: Boolean = executeRequestHandler(channel = connection, requestHandler)
			if (executed) {
				SinglyTryResult.Handled
			} else {
				SinglyTryResult.Invalid
			}
		}
	}

	override fun close() {
		this.channel.closeAndDeleteUnixDomainSocket()
	}
}

private suspend fun executeRequestHandler(channel: SocketChannel, requestHandler: RequestHandler): Boolean {
	val requestTypeResult: ReadResult<ValidationResult<RequestType>> =
		withContext(Dispatchers.IO) {
			channel.deserialize(MagicNumberAndRequestTypeDeserializer)
		}

	val requestType: RequestType = requestTypeResult.getValidOrNull()
		?: return false

	when (requestType) {
		RequestType.Status -> executeStatusRequestHandler(channel, requestHandler)
		RequestType.Pause -> return executePauseRequestHandler(channel, requestHandler)
		RequestType.Resume -> executeResumeRequestHandler(channel, requestHandler)
		RequestType.Shutdown -> executeShutdownRequestHandler(channel, requestHandler)
	}

	return true
}

// region status

private suspend fun executeStatusRequestHandler(channel: SocketChannel, requestHandler: RequestHandler) {
	StatusRequestHandle(channel).use { handle: StatusRequestHandle ->
		val request = Request.Status(handle)
		requestHandler.handleRequest(request)
	}
}

private class StatusRequestHandle(private val channel: SocketChannel) : RequestHandle.Status, Closeable {

	@Volatile
	private var closed: Boolean = false

	override suspend fun sendCurrentState(currentState: BotState) {
		check(!(this.closed)) {
			"This request handle is closed"
		}

		withContext(Dispatchers.IO) {
			channel.serialize(BotStateSerializer, currentState)
		}
	}

	override fun close() {
		this.closed = true
	}
}

// endregion

// region pause

private suspend fun executePauseRequestHandler(channel: SocketChannel, requestHandler: RequestHandler): Boolean {
	val reasonResult: ReadResult<ValidationResult<BotPauseReason?>> =
		withContext(Dispatchers.IO) {
			channel.deserialize(OptionalBotPauseReasonDeserializer)
		}

	val reason: BotPauseReason? = (reasonResult.getOrNull() ?: return false).getOrElse { return false }

	PauseRequestHandle(channel).use { handle: PauseRequestHandle ->
		val request = Request.Pause(reason, handle)
		requestHandler.handleRequest(request)
	}

	return true
}

private class PauseRequestHandle(private val channel: SocketChannel) : RequestHandle.Pause, Closeable {

	@Volatile
	private var closed: Boolean = false

	override suspend fun sendOldState(oldState: BotState) {
		check(!(this.closed)) {
			"This request handle is closed"
		}

		withContext(Dispatchers.IO) {
			channel.serialize(BotStateSerializer, oldState)
		}
	}

	override fun close() {
		this.closed = true
	}
}

// endregion

// region resume

private suspend fun executeResumeRequestHandler(channel: SocketChannel, requestHandler: RequestHandler) {
	ResumeRequestHandle(channel).use { handle: ResumeRequestHandle ->
		val request = Request.Resume(handle)
		requestHandler.handleRequest(request)
	}
}

private class ResumeRequestHandle(private val channel: SocketChannel) : RequestHandle.Resume, Closeable {

	@Volatile
	private var closed: Boolean = false

	override suspend fun sendOldState(oldState: BotState) {
		check(!(this.closed)) {
			"This request handle is closed"
		}

		withContext(Dispatchers.IO) {
			channel.serialize(BotStateSerializer, oldState)
		}
	}

	override fun close() {
		this.closed = true
	}
}

// endregion

// region shutdown

private suspend fun executeShutdownRequestHandler(channel: SocketChannel, requestHandler: RequestHandler) {
	ShutdownRequestHandle(channel).use { handle: ShutdownRequestHandle ->
		val request = Request.Shutdown(handle)
		requestHandler.handleRequest(request)
	}
}

private class ShutdownRequestHandle(private val channel: SocketChannel) : RequestHandle.Shutdown, Closeable {

	@Volatile
	private var closed: Boolean = false

	override suspend fun sendShutdownFinished() {
		check(!(this.closed)) {
			"This request handle is closed"
		}

		withContext(Dispatchers.IO) {
			channel.serialize(ShutdownFinishedMessageSerializer)
		}
	}

	override fun close() {
		this.closed = true
	}
}

// endregion
