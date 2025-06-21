package io.github.maybeashleyidk.remotecontroldiscordbot

@JvmInline
internal value class BotToken private constructor(private val tokenString: String) {

	override fun toString(): String {
		return this.tokenString
	}

	companion object {

		fun ofString(tokenString: String): BotToken? {
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
