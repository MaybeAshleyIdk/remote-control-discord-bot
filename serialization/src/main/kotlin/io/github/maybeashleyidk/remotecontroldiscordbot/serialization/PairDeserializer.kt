package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public infix fun <A, B> Deserializer<A>.then(secondDeserializer: Deserializer<B>): Deserializer<Pair<A, B>> {
	return PairDeserializer(firstDeserializer = this, secondDeserializer)
}

private class PairDeserializer<A, B>(
	private val firstDeserializer: Deserializer<A>,
	private val secondDeserializer: Deserializer<B>,
) : Deserializer<Pair<A, B>>(
	elementName = "Pair of ${firstDeserializer.elementName} and ${secondDeserializer.elementName}",
	elementMinSize = firstDeserializer.elementMinSize + secondDeserializer.elementMinSize,
) {

	override fun readFrom(source: Source): Pair<A, B> {
		val firstElement: A = this.firstDeserializer.readFrom(source)
		val secondElement: B = this.secondDeserializer.readFrom(source)
		return (firstElement to secondElement)
	}
}
