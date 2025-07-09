package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal.throwHigherPriorityThrowable
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server.SocketIpcServerFactory.BindResult
import java.net.BindException
import java.net.StandardProtocolFamily
import java.net.UnixDomainSocketAddress
import java.nio.channels.ServerSocketChannel
import java.nio.file.Path
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import kotlin.io.path.createParentDirectories
import kotlin.io.path.setPosixFilePermissions

private val userOnlyRwxPermissions: Set<PosixFilePermission> = PosixFilePermissions.fromString("rwx------")

public fun SocketIpcServerFactory(socketPath: Path): SocketIpcServerFactory {
	return SocketIpcServerFactoryImpl(socketPath)
}

private class SocketIpcServerFactoryImpl(socketPath: Path) : SocketIpcServerFactory {

	private val socketAddress: UnixDomainSocketAddress = UnixDomainSocketAddress.of(socketPath)

	override fun bind(): BindResult {
		val channel: ServerSocketChannel = ServerSocketChannel.open(StandardProtocolFamily.UNIX)

		this.socketAddress.path.createParentDirectories(PosixFilePermissions.asFileAttribute(userOnlyRwxPermissions))

		try {
			channel.bind(this.socketAddress)
		} catch (_: BindException) {
			channel.close()
			return BindResult.SocketAlreadyExists
		}

		try {
			(channel.localAddress as? UnixDomainSocketAddress)?.path?.setPosixFilePermissions(userOnlyRwxPermissions)

			Runtime.getRuntime()
				.addShutdownHook(Thread(channel::closeAndDeleteUnixDomainSocket))

			val server = SocketIpcServerImpl(channel)
			return BindResult.Success(server)
		} catch (e: Throwable) {
			try {
				channel.closeAndDeleteUnixDomainSocket()
			} catch (closeException: Throwable) {
				throwHigherPriorityThrowable(e, closeException)
			}

			throw e
		}
	}
}
