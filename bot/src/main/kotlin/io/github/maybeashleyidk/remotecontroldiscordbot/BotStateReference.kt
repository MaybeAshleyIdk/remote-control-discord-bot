package io.github.maybeashleyidk.remotecontroldiscordbot

/**
 * A reference to a [BotState].
 *
 * The operations of this interface are atomic.
 */
public interface BotStateReference {

	public fun load(): BotState

	public fun compareAndSet(expectedState: BotState, newState: BotState): Boolean

	public fun exchange(newState: BotState): BotState
}
