package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public abstract class Serializer<in E>(public val elementName: String) {

	public abstract fun write(element: E, sink: Sink)

	public companion object {

		public fun Serializer<Unit>.writeInto(sink: Sink) {
			this.write(element = Unit, sink)
		}
	}
}
