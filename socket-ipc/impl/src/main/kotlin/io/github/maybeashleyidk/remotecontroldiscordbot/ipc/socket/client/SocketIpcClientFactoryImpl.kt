package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client.SocketIpcClientFactory.ConnectResult
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.throwHigherPriorityThrowable
import java.net.SocketException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.SocketChannel
import java.nio.file.Path

public fun SocketIpcClientFactory(socketPath: Path): SocketIpcClientFactory {
	return SocketIpcClientFactoryImpl(socketPath)
}

private class SocketIpcClientFactoryImpl(socketPath: Path) : SocketIpcClientFactory {

	private val socketAddress: UnixDomainSocketAddress = UnixDomainSocketAddress.of(socketPath)

	override fun connect(): ConnectResult {
		val channel: SocketChannel = SocketChannel.open(StandardProtocolFamily.UNIX)

		try {
			channel.connect(this.socketAddress)
		} catch (_: SocketException) {
			channel.close()
			return ConnectResult.NoSuchSocket
		}

		try {
			val client = SocketIpcClientImpl(channel)
			return ConnectResult.Success(client)
		} catch (e: Throwable) {
			try {
				channel.close()
			} catch (closeException: Exception) {
				throwHigherPriorityThrowable(e, closeException)
			}

			throw e
		}
	}
}
