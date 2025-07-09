package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public abstract class Deserializer<out E>(
	public val elementName: String,
	public val elementMinSize: Int,
) {

	public abstract fun readFrom(source: Source): E
}
