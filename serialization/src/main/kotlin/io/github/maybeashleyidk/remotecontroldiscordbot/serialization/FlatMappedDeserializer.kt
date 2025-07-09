package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public fun <T, R> Deserializer<T>.then(elementName: String, transform: (T) -> Deserializer<R>): Deserializer<R> {
	return FlatMappedDeserializer(upstreamDeserializer = this, elementName, transform)
}

private class FlatMappedDeserializer<T, R>(
	private val upstreamDeserializer: Deserializer<T>,
	elementName: String,
	private val transform: (T) -> Deserializer<R>,
) : Deserializer<R>(elementName = elementName, elementMinSize = upstreamDeserializer.elementMinSize) {

	override fun readFrom(source: Source): R {
		val upstreamElement: T = this.upstreamDeserializer.readFrom(source)
		val mappedDeserializer: Deserializer<R> = this.transform(upstreamElement)
		return mappedDeserializer.readFrom(source)
	}
}
