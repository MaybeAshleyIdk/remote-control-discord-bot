package io.github.maybeashleyidk.remotecontroldiscordbot.env

import io.github.maybeashleyidk.remotecontroldiscordbot.InstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.ProcessInformation
import java.nio.file.Path
import kotlin.io.path.div

internal fun getInstanceConfigDirectoryPathOrExit(
	processInformation: ProcessInformation,
	instanceName: InstanceName,
): Path {
	return getBaseConfigDirectoryPathOrExit(processInformation) /
		"remote-control-discord-bot" / "instances" / instanceName.toString()
}

private fun getBaseConfigDirectoryPathOrExit(processInformation: ProcessInformation): Path {
	return System.getenv("XDG_CONFIG_HOME")
		?.ifEmpty { null }
		?.let(Path::of)
		?.takeIf(Path::isAbsolute)
		?: (getHomeDirectoryPathOrExit(processInformation) / ".config")
}
