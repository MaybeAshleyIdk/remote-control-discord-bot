package io.github.maybeashleyidk.remotecontroldiscordbot.env

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.XdgRuntimeDirEnvironmentVariableNotAbsolute
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.XdgRuntimeDirEnvironmentVariableUnsetOrEmpty
import io.github.maybeashleyidk.remotecontroldiscordbot.InstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.ProcessInformation
import io.github.maybeashleyidk.remotecontroldiscordbot.exitProcess
import java.nio.file.Path
import kotlin.io.path.div

internal fun getInstanceRuntimeDirectoryPathOrExit(
	processInformation: ProcessInformation,
	instanceName: InstanceName,
): Path {
	return getBaseRuntimeDirectoryPathOrExit(processInformation) /
		"remote-control-discord-bot" / "instances" / instanceName.toString()
}

private fun getBaseRuntimeDirectoryPathOrExit(processInformation: ProcessInformation): Path {

	val baseRuntimeDirectoryPathStr: String? = System.getenv("XDG_RUNTIME_DIR")?.ifEmpty { null }

	if (baseRuntimeDirectoryPathStr.isNullOrEmpty()) {
		val message: String = "${processInformation.stderrLoggingTag}: the environment variable \$XDG_RUNTIME_DIR is " +
			"unset or empty"
		System.err.println(message)

		exitProcess(XdgRuntimeDirEnvironmentVariableUnsetOrEmpty)
	}

	val baseRuntimeDirectoryPath: Path = Path.of(baseRuntimeDirectoryPathStr)

	if (!(baseRuntimeDirectoryPath.isAbsolute)) {
		val message: String = "${processInformation.stderrLoggingTag}: the environment variable \$XDG_RUNTIME_DIR is " +
			"not absolute"
		System.err.println(message)

		exitProcess(XdgRuntimeDirEnvironmentVariableNotAbsolute)
	}

	return baseRuntimeDirectoryPath
}
