package io.github.maybeashleyidk.remotecontroldiscordbot.internal.state

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.BotStateReference
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo

internal class LoggingBotStateReference(
	private val reference: BotStateReference,
	private val logger: Logger,
) : BotStateReference {

	override fun load(): BotState {
		return this.reference.load()
	}

	override fun compareAndSet(expectedState: BotState, newState: BotState): Boolean {
		val success: Boolean = this.reference.compareAndSet(expectedState, newState)

		if (success) {
			this.logNewState(oldState = expectedState, newState)
		}

		return success
	}

	private fun logNewState(oldState: BotState, newState: BotState) {
		val message: String = createLoggingMessage(oldState, newState)

		if (message.isNotEmpty()) {
			this.logger.logInfo(message)
		}
	}
}

private fun createLoggingMessage(oldState: BotState, newState: BotState): String {
	return when (oldState) {
		is BotState.Resumed -> when (newState) {
			is BotState.Resumed -> ""

			is BotState.Paused -> {
				val reasonInfo: String = newState.reason
					?.let { "with the reason \"$it\"" }
					?: "without a reason"

				"The bot was paused $reasonInfo"
			}
		}

		is BotState.Paused -> when (newState) {
			is BotState.Resumed -> {
				val oldStateInfo: String = oldState.reason
					?.let { "the previous pause reason was \"$it\"" }
					?: "there previously was no pause reason"

				"The bot was resumed ($oldStateInfo)"
			}

			is BotState.Paused -> {
				if (oldState.reason != newState.reason) {
					val newStateInfo: String = newState.reason
						?.let { "The bot pause reason was changed to \"$it\"" }
						?: "The bot pause reason was removed"

					val oldStateInfo: String = oldState.reason
						?.let { "the previous reason was \"$it\"" }
						?: "there previously was no reason"

					"$newStateInfo ($oldStateInfo)"
				} else {
					""
				}
			}
		}
	}
}
