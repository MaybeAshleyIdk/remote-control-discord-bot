package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client.ResponseHandle
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client.SocketIpcClient
import io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client.SocketIpcClientFactory
import kotlinx.coroutines.runBlocking
import java.nio.file.Path

internal fun mainIpcClient(processInformation: ProcessInformation, instanceName: InstanceName) {
	val ipcSocketPath: Path = getIpcSocketPathOrExit(processInformation, instanceName)

	val clientFactory = SocketIpcClientFactory(ipcSocketPath)

	when (val result: SocketIpcClientFactory.ConnectResult = clientFactory.connect()) {
		is SocketIpcClientFactory.ConnectResult.Success -> result.client
		SocketIpcClientFactory.ConnectResult.NoSuchSocket -> error("no such socket")
	}.use { client: SocketIpcClient ->
		runBlocking {
			val handle: ResponseHandle.Status = client.sendStatusRequest()
			println("ACK")
			val state = handle.receiveCurrentState()
			println("state: $state")
		}
	}
}
