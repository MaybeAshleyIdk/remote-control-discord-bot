package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import net.dv8tion.jda.api.JDA as Jda
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

internal suspend fun Jda.suspendUntilShutdown() {
	suspendCancellableCoroutine { continuation: CancellableContinuation<Unit> ->
		val shutdownAwaitingThread =
			Thread {
				try {
					this.awaitShutdown()
				} catch (e: InterruptedException) {
					continuation.cancel(e)
					return@Thread
				} catch (e: Throwable) {
					continuation.resumeWithException(e)
					return@Thread
				}

				continuation.resume(Unit)
			}

		shutdownAwaitingThread.start()

		continuation.invokeOnCancellation {
			shutdownAwaitingThread.interrupt()
		}
	}
}
