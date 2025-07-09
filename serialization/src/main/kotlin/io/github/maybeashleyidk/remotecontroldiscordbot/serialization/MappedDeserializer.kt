package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public fun <T, R> Deserializer<T>.map(elementName: String, transform: (element: T) -> R): Deserializer<R> {
	return MappedDeserializer(upstreamDeserializer = this, elementName, transform)
}

private class MappedDeserializer<T, R>(
	private val upstreamDeserializer: Deserializer<T>,
	elementName: String,
	private val transform: (element: T) -> R,
) : Deserializer<R>(elementName = elementName, elementMinSize = upstreamDeserializer.elementMinSize) {

	override fun readFrom(source: Source): R {
		val upstreamElement: T = this.upstreamDeserializer.readFrom(source)
		return this.transform(upstreamElement)
	}
}
