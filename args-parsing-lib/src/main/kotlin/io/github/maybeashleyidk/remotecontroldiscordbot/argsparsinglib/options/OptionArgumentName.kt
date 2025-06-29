package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.PlaceholderName

@JvmInline
public value class OptionArgumentName(private val placeholderName: PlaceholderName) {

	override fun toString(): String {
		return this.placeholderName.toString()
	}

	public companion object {

		public fun String.toOptionArgumentName(): OptionArgumentName {
			val placeholderName: PlaceholderName? = PlaceholderName.ofString(nameString = this)

			requireNotNull(placeholderName) {
				"Invalid option argument name string: \"$this\""
			}

			return OptionArgumentName(placeholderName)
		}
	}
}
