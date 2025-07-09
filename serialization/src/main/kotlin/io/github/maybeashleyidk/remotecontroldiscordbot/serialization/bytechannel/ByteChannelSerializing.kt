package io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.WritableByteChannel

private sealed class Write {
	class Byte(val value: kotlin.Byte) : Write()
	class Int(val value: kotlin.Int) : Write()
	class Long(val value: kotlin.Long) : Write()
	class ByteArray(val value: kotlin.ByteArray) : Write()
}

public fun <T> WritableByteChannel.serialize(serializer: Serializer<T>, value: T) {
	val buffer: ByteBuffer = serializer.createByteBufferOf(value)

	val n: Int =
		try {
			this.write(buffer)
		} catch (e: ClosedChannelException) {
			throw e
		} catch (e: IOException) {
			throw IOException("Failed to write a ${serializer.elementName}", e)
		}

	val capacity: Int = buffer.capacity()
	if (n != capacity) {
		val message: String = "Failed to write a ${serializer.elementName}: " +
			"Expected to write $capacity bytes, but actually only wrote $n bytes"

		throw IOException(message)
	}
}

public fun WritableByteChannel.serialize(serializer: Serializer<Unit>) {
	this.serialize(serializer, value = Unit)
}

private fun <T> Serializer<T>.createByteBufferOf(element: T): ByteBuffer {
	val writes: ImmutableList<Write> =
		collectWrites { sink: Sink ->
			this.write(element, sink)
		}

	val size: Int = writes
		.sumOf { write: Write ->
			when (write) {
				is Write.Byte -> Byte.SIZE_BYTES
				is Write.Int -> Int.SIZE_BYTES
				is Write.Long -> Long.SIZE_BYTES
				is Write.ByteArray -> write.value.size
			}
		}

	val buffer: ByteBuffer = ByteBuffer.allocate(size)

	for (write: Write in writes) {
		when (write) {
			is Write.Byte -> buffer.put(write.value)
			is Write.Int -> buffer.putInt(write.value)
			is Write.Long -> buffer.putLong(write.value)
			is Write.ByteArray -> buffer.put(write.value)
		}
	}

	buffer.rewind()

	return buffer
}

private inline fun collectWrites(block: (sink: Sink) -> Unit): ImmutableList<Write> {
	val sink = WriteCollectingSink()

	block(sink)

	return sink.getWritesAndClose()
}

private class WriteCollectingSink : Sink {

	private var writes: MutableList<Write>? = mutableListOf()

	override fun writeByte(element: Byte) {
		this.addWriteEnsureNotClosed(Write.Byte(element))
	}

	override fun writeInt(element: Int) {
		this.addWriteEnsureNotClosed(Write.Int(element))
	}

	override fun writeLong(element: Long) {
		this.addWriteEnsureNotClosed(Write.Long(element))
	}

	override fun writeByteArray(element: ByteArray) {
		this.addWriteEnsureNotClosed(Write.ByteArray(element))
	}

	fun getWritesAndClose(): ImmutableList<Write> {
		val writes: ImmutableList<Write>? = this.writes?.toImmutableList()

		this.writes = null

		return checkNotNull(writes) {
			"The sink is already closed"
		}
	}

	private fun addWriteEnsureNotClosed(write: Write) {
		val writes: MutableList<Write> =
			checkNotNull(this.writes) {
				"This sink is closed"
			}

		writes += write
	}
}
