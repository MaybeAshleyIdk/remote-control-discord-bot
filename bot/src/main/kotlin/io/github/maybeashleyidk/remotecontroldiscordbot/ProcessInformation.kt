package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.OperatingSystem
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import java.nio.file.Paths

internal data class ProcessInformation(
	val usageExecutableArguments: ImmutableList<String>,
	val stderrLoggingTag: String,
) {

	val usageExecutableArgumentsString: String
		get() {
			return this.usageExecutableArguments
				.joinToString(separator = " ")
		}

	companion object {

		fun determine(mainClass: Class<*>): ProcessInformation {
			return this.determineFromJarExecScript()
				?: this.determineFromExecutingJar(mainClass)
				?: this.ofString("remote-control-discord-bot")
		}

		private fun determineFromJarExecScript(): ProcessInformation? {
			return System.getProperty("remoteControlDiscordBot.jarExecScript.executableName")
				?.ifEmpty { null }
				?.let(this::ofString)
		}

		private fun determineFromExecutingJar(mainClass: Class<*>): ProcessInformation? {
			val jarFilePath: String = mainClass
				.protectionDomain
				.codeSource
				?.location
				?.takeIf { it.protocol == "file" }
				?.path
				?.let { Paths.get(it) }
				?.let { Paths.get(".").toAbsolutePath().relativize(it) }
				?.toString()
				?: return null

			val javaExecutableName: String =
				when (OperatingSystem.determine()) {
					null -> return null
					OperatingSystem.UnixLike -> "java"
					OperatingSystem.MicrosoftWindows -> "java.exe"
				}

			return ProcessInformation(
				usageExecutableArguments = persistentListOf(javaExecutableName, "-jar", jarFilePath),
				stderrLoggingTag = jarFilePath,
			)
		}

		private fun ofString(string: String): ProcessInformation {
			return ProcessInformation(usageExecutableArguments = persistentListOf(string), stderrLoggingTag = string)
		}
	}
}
