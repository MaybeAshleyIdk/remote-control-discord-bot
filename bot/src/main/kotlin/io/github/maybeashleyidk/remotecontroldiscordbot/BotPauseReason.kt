package io.github.maybeashleyidk.remotecontroldiscordbot

/**
 * The reason why the bot was paused.
 *
 * This will be both logged internally and displayed to a user trying to execute a slash command.
 *
 * It has no length restrictions, however, if a reason is too long to display (Discord has a message limit of
 * 2000 Unicode code points) then it will be truncated.
 */
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
