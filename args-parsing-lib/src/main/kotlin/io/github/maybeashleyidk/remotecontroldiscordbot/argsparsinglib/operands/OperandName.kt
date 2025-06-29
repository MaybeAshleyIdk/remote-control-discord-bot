package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands

import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.PlaceholderName

@JvmInline
public value class OperandName(private val placeholderName: PlaceholderName) {

	override fun toString(): String {
		return this.placeholderName.toString()
	}

	public companion object {

		public fun String.toOperandName(): OperandName {
			val placeholderName: PlaceholderName? = PlaceholderName.Companion.ofString(nameString = this)

			requireNotNull(placeholderName) {
				"Invalid operand name string: \"$this\""
			}

			return OperandName(placeholderName)
		}
	}
}
