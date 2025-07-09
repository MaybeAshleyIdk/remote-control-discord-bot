package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public object EmptyDeserializer : Deserializer<Unit>(elementName = "nothing", elementMinSize = 0) {

	override fun readFrom(source: Source) {}
}

public object ByteDeserializer : Deserializer<Byte>(elementName = "Byte", elementMinSize = Byte.SIZE_BYTES) {

	override fun readFrom(source: Source): Byte {
		return source.readByte()
	}
}

public object IntDeserializer : Deserializer<Int>(elementName = "Int", elementMinSize = Int.SIZE_BYTES) {

	override fun readFrom(source: Source): Int {
		return source.readInt()
	}
}

public object LongDeserializer : Deserializer<Long>(elementName = "Long", elementMinSize = Long.SIZE_BYTES) {

	override fun readFrom(source: Source): Long {
		return source.readLong()
	}
}

public class ByteArrayDeserializer(
	private val size: Int,
) : Deserializer<ByteArray>(elementName = "ByteArray with size $size", elementMinSize = size) {

	override fun readFrom(source: Source): ByteArray {
		return source.readByteArray(size = this.size)
	}
}
