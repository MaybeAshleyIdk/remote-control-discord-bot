package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

@JvmInline
public value class OptionArgumentDefinition(public val name: OptionArgumentName) {

	override fun toString(): String {
		return "<${this.name}>"
	}
}
