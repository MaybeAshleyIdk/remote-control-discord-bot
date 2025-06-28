package io.github.maybeashleyidk.remotecontroldiscordbot.internal.state

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.BotStateReference
import java.util.concurrent.atomic.AtomicReference

internal class JavaAtomicBotStateReference(initialState: BotState) : BotStateReference {

	private val atomicReference: AtomicReference<BotState> = AtomicReference(initialState)

	override fun load(): BotState {
		return this.atomicReference.get()
	}

	override fun compareAndSet(expectedState: BotState, newState: BotState): Boolean {
		return this.atomicReference.compareAndSet(expectedState, newState)
	}
}
