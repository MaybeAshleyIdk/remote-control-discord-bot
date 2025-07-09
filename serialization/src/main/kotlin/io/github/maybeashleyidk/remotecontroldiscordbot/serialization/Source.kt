package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public interface Source {

	public fun readByte(): Byte

	public fun readInt(): Int

	public fun readLong(): Long

	public fun readByteArray(size: Int): ByteArray
}
