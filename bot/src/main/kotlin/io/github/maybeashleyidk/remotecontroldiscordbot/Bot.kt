package io.github.maybeashleyidk.remotecontroldiscordbot

import kotlinx.coroutines.InternalForInheritanceCoroutinesApi
import kotlinx.coroutines.Job

@OptIn(InternalForInheritanceCoroutinesApi::class)
public data class Bot(
	private val job: Job,
	public val stateReference: BotStateReference,
) : Job by job
