package io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Source
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.ReadableByteChannel

public fun <E> ReadableByteChannel.deserialize(deserializer: Deserializer<E>): ReadResult<E> {
	require(deserializer.elementMinSize >= 1) {
		"Deserializer's element minimum size must be greater than- or equal to 1"
	}

	val buffer: ByteBuffer = ByteBuffer.allocate(deserializer.elementMinSize)

	val n: Int =
		try {
			this.read(buffer)
		} catch (e: ClosedChannelException) {
			return ReadResult.Closed(exception = e)
		} catch (e: IOException) {
			return ReadResult.Failure(
				elementName = deserializer.elementName,
				error = e,
			)
		}

	val capacity: Int = buffer.capacity()
	if (n != capacity) {
		return ReadResult.Incomplete(
			elementName = deserializer.elementName,
			expected = capacity,
			actual = n,
		)
	}

	buffer.rewind()

	val element: E = deserializer.readFrom(source = buffer.asSource())

	return ReadResult.Success(element)
}

private fun ByteBuffer.asSource(): Source {
	return ByteBufferSource(buffer = this)
}

private class ByteBufferSource(private val buffer: ByteBuffer) : Source {

	override fun readByte(): Byte {
		return this.buffer.get()
	}

	override fun readInt(): Int {
		return this.buffer.getInt()
	}

	override fun readLong(): Long {
		return this.buffer.getLong()
	}

	override fun readByteArray(size: Int): ByteArray {
		if (size == 0) {
			return byteArrayOf()
		}

		val capacity: Int = this.buffer.capacity()
		return if (capacity == size) {
			this.buffer.position(capacity)
			this.buffer.array()
		} else {
			val bytes = ByteArray(size)
			this.buffer.get(bytes)
			bytes
		}
	}
}
