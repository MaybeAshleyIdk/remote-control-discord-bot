package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

private const val ELEMENT_NAME: String = "String"

public val StringSerializer: Serializer<String> =
	Serializer(elementName = ELEMENT_NAME) { string: String, sink: Sink ->
		val bytes: ByteArray = string.encodeToByteArray()

		sink.writeInt(bytes.size)
		sink.writeByteArray(bytes)
	}

public val StringDeserializer: Deserializer<String> =
	IntDeserializer.then(elementName = ELEMENT_NAME) { bytesSize: Int ->
		ByteArrayDeserializer(size = bytesSize)
			.map(
				elementName = "UTF-8 $ELEMENT_NAME with $bytesSize bytes",
				transform = ByteArray::decodeToString,
			)
	}
