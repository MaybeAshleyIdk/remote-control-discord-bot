package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ByteDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map

private const val ELEMENT_NAME: String = "shutdown-finished message"

internal val ShutdownFinishedMessageSerializer: Serializer<Unit> =
	Serializer(elementName = ELEMENT_NAME) { sink: Sink ->
		sink.writeByte(1)
	}

internal val ShutdownFinishedMessageDeserializer: Deserializer<ValidationResult<Unit>> = ByteDeserializer
	.map(elementName = ELEMENT_NAME) { b: Byte ->
		if (b == 1.toByte()) {
			ValidationResult.Valid(Unit)
		} else {
			ValidationResult.Invalid("Expected byte with value 1, but got $b")
		}
	}
