package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

@Suppress("NOTHING_TO_INLINE")
internal inline fun throwHigherPriorityThrowable(first: Throwable, second: Throwable): Nothing {
	throw when {
		first !is Exception -> first.withSuppressed(second)
		second !is Exception -> second.withSuppressed(first)
		else -> first.withSuppressed(second)
	}
}

@Suppress("NOTHING_TO_INLINE")
private inline fun <T : Throwable> T.withSuppressed(exception: Throwable): T {
	this.addSuppressed(exception)
	return this
}
