package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason
import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.BotStateDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.MagicNumberAndRequestTypeSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.OptionalBotPauseReasonSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.RequestType
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.ShutdownFinishedMessageDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.ReadResult.Companion.getValidOrThrow
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.deserialize
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel.serialize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.channels.SocketChannel

private val PauseRequestSerializer: Serializer<BotPauseReason?> =
	Serializer(elementName = "pause request") { pauseReason: BotPauseReason?, sink: Sink ->
		MagicNumberAndRequestTypeSerializer.write(RequestType.Pause, sink)
		OptionalBotPauseReasonSerializer.write(pauseReason, sink)
	}

internal class SocketIpcClientImpl(private val channel: SocketChannel) : SocketIpcClient {

	override suspend fun sendStatusRequest(): ResponseHandle.Status {
		withContext(Dispatchers.IO) {
			channel.serialize(MagicNumberAndRequestTypeSerializer, RequestType.Status)
		}

		return StatusResponseHandle(channel = this.channel)
	}

	override suspend fun sendPauseRequest(reason: BotPauseReason?): ResponseHandle.Pause {
		withContext(Dispatchers.IO) {
			channel.serialize(PauseRequestSerializer, reason)
		}

		return PauseResponseHandle(channel = this.channel)
	}

	override suspend fun sendResumeRequest(): ResponseHandle.Resume {
		withContext(Dispatchers.IO) {
			channel.serialize(MagicNumberAndRequestTypeSerializer, RequestType.Resume)
		}

		return ResumeResponseHandle(channel = this.channel)
	}

	override suspend fun sendShutdownRequest(): ResponseHandle.Shutdown {
		withContext(Dispatchers.IO) {
			channel.serialize(MagicNumberAndRequestTypeSerializer, RequestType.Shutdown)
		}

		return ShutdownResponseHandle(channel = this.channel)
	}

	override fun close() {
		this.channel.close()
	}
}

private class StatusResponseHandle(private val channel: SocketChannel) : ResponseHandle.Status {

	override suspend fun receiveCurrentState(): BotState {
		return withContext(Dispatchers.IO) {
			channel.deserialize(BotStateDeserializer)
		}.getValidOrThrow()
	}
}

private class PauseResponseHandle(private val channel: SocketChannel) : ResponseHandle.Pause {

	override suspend fun receiveOldState(): BotState {
		return withContext(Dispatchers.IO) {
			channel.deserialize(BotStateDeserializer)
		}.getValidOrThrow()
	}
}

private class ResumeResponseHandle(private val channel: SocketChannel) : ResponseHandle.Resume {

	override suspend fun receiveOldState(): BotState {
		return withContext(Dispatchers.IO) {
			channel.deserialize(BotStateDeserializer)
		}.getValidOrThrow()
	}
}

private class ShutdownResponseHandle(private val channel: SocketChannel) : ResponseHandle.Shutdown {

	override suspend fun receiveShutdownFinished() {
		withContext(Dispatchers.IO) {
			channel.deserialize(ShutdownFinishedMessageDeserializer)
		}.getValidOrThrow()
	}
}
