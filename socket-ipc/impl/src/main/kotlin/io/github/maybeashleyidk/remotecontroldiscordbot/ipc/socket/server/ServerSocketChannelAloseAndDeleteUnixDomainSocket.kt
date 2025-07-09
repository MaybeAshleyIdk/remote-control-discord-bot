package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.throwHigherPriorityThrowable
import java.net.SocketAddress
import java.net.UnixDomainSocketAddress
import java.nio.channels.ClosedChannelException
import java.nio.channels.ServerSocketChannel
import kotlin.io.path.deleteIfExists

internal fun ServerSocketChannel.closeAndDeleteUnixDomainSocket() {
	val localAddress: SocketAddress? =
		try {
			this.localAddress
		} catch (_: ClosedChannelException) {
			// ServerSocketChannel implements Closeable, which requires its close() function to be idempotent, so
			// this function should also be idempotent and may be called multiple times.
			return
		}

	var closeException: Throwable? = null
	try {
		this.close()
	} catch (e: Throwable) {
		closeException = e
	}

	try {
		(localAddress as? UnixDomainSocketAddress?)?.path?.deleteIfExists()
	} catch (e: Throwable) {
		if (closeException == null) {
			throw e
		}

		throwHigherPriorityThrowable(closeException, e)
	}
}
