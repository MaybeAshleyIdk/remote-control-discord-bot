package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib

@JvmInline
public value class PlaceholderName private constructor(private val nameString: String) {

	init {
		require(nameString.isPlaceholderName())
	}

	override fun toString(): String {
		return this.nameString
	}

	public companion object {

		public fun ofString(nameString: String): PlaceholderName? {
			if (!(nameString.isPlaceholderName())) {
				return null
			}

			return PlaceholderName(nameString)
		}
	}
}

private fun String.isPlaceholderName(): Boolean {
	return this.isNotEmpty() && this.splitToSequence('_')
		.all { substring: String ->
			substring.isNotEmpty() && substring.all { ch: Char ->
				(ch in 'a'..'z') || (ch in '0'..'9')
			}
		}
}
