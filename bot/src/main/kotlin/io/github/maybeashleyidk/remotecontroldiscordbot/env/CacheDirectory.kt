package io.github.maybeashleyidk.remotecontroldiscordbot.env

import io.github.maybeashleyidk.remotecontroldiscordbot.InstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.ProcessInformation
import java.nio.file.Path
import kotlin.io.path.div

internal fun getInstanceCacheDirectoryPathOrExit(
	processInformation: ProcessInformation,
	instanceName: InstanceName,
): Path {
	return getBaseCacheDirectoryPathOrExit(processInformation) /
		"remote-control-discord-bot" / "instances" / instanceName.toString()
}

private fun getBaseCacheDirectoryPathOrExit(processInformation: ProcessInformation): Path {
	return System.getenv("XDG_CACHE_HOME")
		?.ifEmpty { null }
		?.let(Path::of)
		?.takeIf(Path::isAbsolute)
		?: (getHomeDirectoryPathOrExit(processInformation) / ".cache")
}
