package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public fun <E> Serializer(elementName: String, writeBlock: (element: E, sink: Sink) -> Unit): Serializer<E> {
	return SerializerImpl(elementName, writeBlock)
}

@JvmName("-Serializer")
public inline fun Serializer(elementName: String, crossinline writeBlock: (sink: Sink) -> Unit): Serializer<Unit> {
	return Serializer(elementName) { _: Unit, sink: Sink ->
		writeBlock(sink)
	}
}

private class SerializerImpl<in E>(
	elementName: String,
	private val writeBlock: (element: E, sink: Sink) -> Unit,
) : Serializer<E>(elementName) {

	override fun write(element: E, sink: Sink) {
		this.writeBlock(element, sink)
	}
}
