package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.UuidDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.UuidSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map
import java.util.UUID as Uuid

private const val ELEMENT_NAME: String = "magic number"

// Generated using uuidgen(1) (<https://www.man7.org/linux/man-pages/man1/uuidgen.1.html>)
private val MAGIC_NUMBER: Uuid = Uuid.fromString("5448b67d-31fd-4778-a9e0-4579c66ce8be")

internal val MagicNumberSerializer: Serializer<Unit> =
	Serializer(elementName = ELEMENT_NAME) { sink: Sink ->
		UuidSerializer.write(MAGIC_NUMBER, sink)
	}

internal val MagicNumberDeserializer: Deserializer<ValidationResult<Unit>> = UuidDeserializer
	.map(elementName = ELEMENT_NAME) { uuid: Uuid ->
		if (uuid == MAGIC_NUMBER) {
			ValidationResult.Valid(Unit)
		} else {
			ValidationResult.Invalid("Expected the UUID $MAGIC_NUMBER, but got $uuid")
		}
	}
