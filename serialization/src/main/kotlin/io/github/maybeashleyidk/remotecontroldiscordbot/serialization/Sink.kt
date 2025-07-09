package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public interface Sink {

	public fun writeByte(element: Byte)

	public fun writeInt(element: Int)

	public fun writeLong(element: Long)

	public fun writeByteArray(element: ByteArray)
}
