package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

import java.util.UUID as Uuid

private const val ELEMENT_NAME: String = "UUID"

public val UuidSerializer: Serializer<Uuid> =
	Serializer(elementName = ELEMENT_NAME) { uuid: Uuid, sink: Sink ->
		sink.writeLong(uuid.mostSignificantBits)
		sink.writeLong(uuid.leastSignificantBits)
	}

public val UuidDeserializer: Deserializer<Uuid> = (LongDeserializer then LongDeserializer)
	.map(elementName = ELEMENT_NAME) { (mostSignificantBits: Long, leastSignificantBits: Long) ->
		Uuid(mostSignificantBits, leastSignificantBits)
	}
