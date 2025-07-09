package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer.Companion.writeInto
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.then

private const val ELEMENT_NAME: String = "magic number and request type"

internal val MagicNumberAndRequestTypeSerializer: Serializer<RequestType> =
	Serializer(elementName = ELEMENT_NAME) { requestType: RequestType, sink: Sink ->
		MagicNumberSerializer.writeInto(sink)
		RequestTypeSerializer.write(requestType, sink)
	}

internal val MagicNumberAndRequestTypeDeserializer: Deserializer<ValidationResult<RequestType>> =
	(MagicNumberDeserializer then RequestTypeDeserializer)
		.map(
			elementName = ELEMENT_NAME,
		) { (magicNumberResult: ValidationResult<Unit>, requestTypeResult: ValidationResult<RequestType>) ->
			ValidationResult.merge(
				magicNumberResult,
				requestTypeResult,
			) { _: Unit, requestType: RequestType ->
				requestType
			}
		}
