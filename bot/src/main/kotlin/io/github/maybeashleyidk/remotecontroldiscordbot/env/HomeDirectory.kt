package io.github.maybeashleyidk.remotecontroldiscordbot.env

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.HomeEnvironmentVariableNotAbsolute
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.HomeEnvironmentVariableUnsetOrEmpty
import io.github.maybeashleyidk.remotecontroldiscordbot.ProcessInformation
import io.github.maybeashleyidk.remotecontroldiscordbot.exitProcess
import java.nio.file.Path

internal fun getHomeDirectoryPathOrExit(processInformation: ProcessInformation): Path {
	val homeDirectoryPathStr: String? = System.getenv("HOME")?.ifEmpty { null }

	if (homeDirectoryPathStr.isNullOrEmpty()) {
		System.err.println("${processInformation.stderrLoggingTag}: the environment variable \$HOME is unset or empty")
		exitProcess(HomeEnvironmentVariableUnsetOrEmpty)
	}

	val homeDirectoryPath: Path = Path.of(homeDirectoryPathStr)

	if (!(homeDirectoryPath.isAbsolute)) {
		System.err.println("${processInformation.stderrLoggingTag}: the environment variable \$HOME is not absolute")
		exitProcess(HomeEnvironmentVariableNotAbsolute)
	}

	return homeDirectoryPath
}
