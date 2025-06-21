package io.github.maybeashleyidk.remotecontroldiscordbot

import kotlinx.coroutines.InternalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job

@OptIn(InternalForInheritanceCoroutinesApi::class)
public interface Bot : Job {

	public fun pause(reason: BotPauseReason?)

	public fun resume()
}
