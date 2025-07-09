package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.env.getInstanceRuntimeDirectoryPathOrExit
import java.nio.file.Path
import kotlin.io.path.div

private val IPC_SOCKET_RELATIVE_PATH: Path = Path.of("console")

internal fun getIpcSocketPathOrExit(processInformation: ProcessInformation, instanceName: InstanceName): Path {
	val instanceRuntimeDirectoryPath: Path = getInstanceRuntimeDirectoryPathOrExit(processInformation, instanceName)

	return instanceRuntimeDirectoryPath / IPC_SOCKET_RELATIVE_PATH
}
