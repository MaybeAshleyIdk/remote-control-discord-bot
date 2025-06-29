package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

@JvmInline
public value class ShortOptionIdentifier private constructor(public val character: Char) : OptionIdentifier {

	init {
		require(character.isShortOptionIdentifier())
	}

	override fun toString(): String {
		return "-${this.character}"
	}

	public companion object {

		public fun Char.toShortOptionIdentifier(): ShortOptionIdentifier {
			require(this.isShortOptionIdentifier()) {
				"Invalid short option identifier character: '$this'"
			}

			return ShortOptionIdentifier(character = this)
		}
	}
}

private fun Char.isShortOptionIdentifier(): Boolean {
	return (this in 'a'..'z') ||
		(this in 'A'..'Z') ||
		(this in '0'..'9')
}
