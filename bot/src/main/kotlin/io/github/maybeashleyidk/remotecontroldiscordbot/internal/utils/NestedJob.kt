package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.plus

/**
 * Dummy class to manually create a nested [CoroutineScope].
 *
 * The resulting scope's [Job] will be a child of the parent scope's job.
 *
 * ```
 * val parent: CoroutineScope = ...
 *
 * val child = parent + NestedJob()
 *
 * parent.cancel() // cancels the child scope as well
 * ```
 */
internal class NestedJob

/**
 * Dummy class to manually create a nested [CoroutineScope] with a [SupervisorJob].
 *
 * The resulting scope's `SupervisorJob` will be a child of the parent scope's [Job].
 *
 * ```
 * val parent: CoroutineScope = ...
 *
 * val child = parent + NestedSupervisorJob()
 *
 * parent.cancel() // cancels the child scope as well
 * ```
 */
internal class NestedSupervisorJob

internal operator fun CoroutineScope.plus(@Suppress("unused") job: NestedJob): CoroutineScope {
	return this.createNestedScope(::Job)
}

internal operator fun CoroutineScope.plus(@Suppress("unused") job: NestedSupervisorJob): CoroutineScope {
	return this.createNestedScope(::SupervisorJob)
}

private inline fun CoroutineScope.createNestedScope(crossinline childJobFactory: (parent: Job) -> Job): CoroutineScope {
	val parentJob: Job? = this.coroutineContext[Job]
	requireNotNull(parentJob) {
		"Unable to create a nested scope due to the parent scope ($this) not having a job"
	}

	val childJob: Job = childJobFactory(parentJob)

	return (this + childJob)
}
