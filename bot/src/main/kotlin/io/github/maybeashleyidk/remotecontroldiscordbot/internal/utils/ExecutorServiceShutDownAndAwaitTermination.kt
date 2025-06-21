package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import java.util.concurrent.ExecutorService
import java.util.concurrent.TimeUnit

/**
 * Taken from the [ExecutorService]'s documentation.
 */
internal fun ExecutorService.shutDownAndAwaitTermination() {
	this.shutdown()

	try {
		if (!(this.awaitTermination(60, TimeUnit.SECONDS))) {
			this.shutdownNow()
		}
	} catch (_: InterruptedException) {
		this.shutdownNow()
		Thread.currentThread().interrupt()
	}
}
