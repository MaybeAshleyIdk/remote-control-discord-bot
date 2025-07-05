package io.github.maybeashleyidk.remotecontroldiscordbot.internal.state

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.BotStateReference
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.isActive

internal class CoroutineScopedBotStateReference(
	coroutineScope: CoroutineScope,
	reference: BotStateReference,
) : BotStateReference {

	private data class ActiveData(
		val coroutineScope: CoroutineScope,
		val reference: BotStateReference,
	)

	private companion object {

		fun throwCancellationException(): Nothing {
			throw CancellationException("The bot has shut down")
		}
	}

	@Volatile
	private var activeData: ActiveData? = ActiveData(coroutineScope, reference)

	override fun load(): BotState {
		return this.activeReference.load()
	}

	override fun compareAndSet(expectedState: BotState, newState: BotState): Boolean {
		return this.activeReference.compareAndSet(expectedState, newState)
	}

	override fun exchange(newState: BotState): BotState {
		return this.activeReference.exchange(newState)
	}

	private val activeReference: BotStateReference
		get() {
			val activeData: ActiveData = this.activeData ?: throwCancellationException()

			if (!(activeData.coroutineScope.isActive)) {
				this.activeData = null
				throwCancellationException()
			}

			return activeData.reference
		}
}
