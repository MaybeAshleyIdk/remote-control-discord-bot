package io.github.maybeashleyidk.remotecontroldiscordbot

@JvmInline
public value class BotToken private constructor(private val tokenString: String) {

	override fun toString(): String {
		return this.tokenString
	}

	public companion object {

		public fun ofString(tokenString: String): BotToken? {
			if (!(tokenString.isBotToken())) {
				return null
			}

			return BotToken(tokenString)
		}
	}
}

private fun String.isBotToken(): Boolean {
	return this.isNotEmpty() && this.none(Char::isWhitespace)
}
