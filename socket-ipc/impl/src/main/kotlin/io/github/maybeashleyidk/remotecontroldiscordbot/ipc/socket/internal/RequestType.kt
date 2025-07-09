package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ByteDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map

private const val ELEMENT_NAME: String = "request type"

internal enum class RequestType(val id: Byte) {
	Status(0x10),
	Pause(0x20),
	Resume(0x28),
	Shutdown(0x30),
	;

	companion object {

		fun ofId(id: Byte): RequestType? {
			return entries
				.firstOrNull { type: RequestType ->
					type.id == id
				}
		}
	}
}

internal val RequestTypeSerializer: Serializer<RequestType> =
	Serializer(elementName = ELEMENT_NAME) { requestType: RequestType, sink ->
		sink.writeByte(requestType.id)
	}

internal val RequestTypeDeserializer: Deserializer<ValidationResult<RequestType>> = ByteDeserializer
	.map(elementName = ELEMENT_NAME) { typeId: Byte ->
		val type: RequestType? = RequestType.ofId(typeId)
		if (type != null) {
			ValidationResult.Valid(type)
		} else {
			ValidationResult.Invalid(message = "Invalid request type ID $typeId")
		}
	}
