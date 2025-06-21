package io.github.maybeashleyidk.remotecontroldiscordbot

@JvmInline
public value class BotPauseReason private constructor(private val reasonString: String) {

	init {
		require(reasonString.trim().isNotEmpty())
	}

	override fun toString(): String {
		return this.reasonString
	}

	public companion object {

		public fun ofString(reasonString: String): BotPauseReason? {
			val trimmedString: String = reasonString.trim()

			if (trimmedString.isEmpty()) {
				return null
			}

			return BotPauseReason(trimmedString)
		}
	}
}
