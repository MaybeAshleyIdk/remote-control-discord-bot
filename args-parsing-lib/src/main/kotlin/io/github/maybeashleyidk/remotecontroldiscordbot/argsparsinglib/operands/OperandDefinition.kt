package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands

@JvmInline
public value class OperandDefinition(public val name: OperandName) {

	override fun toString(): String {
		return "<${this.name}>"
	}
}
