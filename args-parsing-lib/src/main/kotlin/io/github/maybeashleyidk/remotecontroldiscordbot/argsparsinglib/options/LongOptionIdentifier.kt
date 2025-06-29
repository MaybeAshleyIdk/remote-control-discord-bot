package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

@JvmInline
public value class LongOptionIdentifier private constructor(public val word: String) : OptionIdentifier {

	init {
		require(word.isLongOptionIdentifier())
	}

	override fun toString(): String {
		return "--${this.word}"
	}

	public companion object {

		public fun String.toLongOptionIdentifier(): LongOptionIdentifier {
			require(this.isLongOptionIdentifier()) {
				"Invalid long option identifier word: \"$this\""
			}

			return LongOptionIdentifier(word = this)
		}
	}
}

private fun String.isLongOptionIdentifier(): Boolean {
	return this.isNotEmpty() && this.splitToSequence('-')
		.all { substring: String ->
			substring.isNotEmpty() && substring.all { ch: Char ->
				(ch in 'a'..'z') || (ch in '0'..'9')
			}
		}
}
