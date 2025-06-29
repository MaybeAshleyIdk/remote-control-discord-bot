package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InvalidToken
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigDuplicateCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigEmptyCommandLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidCommandName
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigZeroCommandsDefined
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.NonExistingFile
import io.github.maybeashleyidk.remotecontroldiscordbot.env.getInstanceConfigDirectoryPathOrExit
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.parseToLocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr.StderrLogger
import kotlinx.coroutines.runBlocking
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText

private val LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH: Path = Path.of("commands.cfg")
private val TOKEN_FILE_RELATIVE_PATH: Path = Path.of("token.txt")

internal fun mainBot(processInformation: ProcessInformation, instanceName: InstanceName) {
	val instanceConfigDirectoryPath: Path = getInstanceConfigDirectoryPathOrExit(processInformation, instanceName)

	val localCommandsConfig: LocalCommandsConfig =
		getLocalCommandsConfigOrExit(processInformation, instanceConfigDirectoryPath)

	val token: BotToken = getTokenOrExit(processInformation, instanceConfigDirectoryPath)

	runBlocking {
		launchBot(token, localCommandsConfig, logger = StderrLogger)
	}
}

private fun getLocalCommandsConfigOrExit(
	processInformation: ProcessInformation,
	instanceConfigDirectoryPath: Path,
): LocalCommandsConfig {
	val configFilePath: Path = instanceConfigDirectoryPath / LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH

	val configFileContents: String =
		try {
			configFilePath.readText()
		} catch (_: NoSuchFileException) {
			System.err.println("${processInformation.stderrLoggingTag}: $configFilePath: no such file")
			exitProcess(NonExistingFile)
		}

	val configFileParsingResult: LocalCommandsConfigParsingResult = configFileContents.parseToLocalCommandsConfig()
	return when (configFileParsingResult) {
		is LocalCommandsConfigParsingResult.Success -> configFileParsingResult.config

		is LocalCommandsConfigParsingResult.Failure -> {
			handleLocalCommandsConfigParsingFailure(
				processInformation,
				configFilePath,
				failure = configFileParsingResult,
			)
		}
	}
}

private fun getTokenOrExit(processInformation: ProcessInformation, instanceConfigDirectoryPath: Path): BotToken {
	val tokenFilePath: Path = instanceConfigDirectoryPath / TOKEN_FILE_RELATIVE_PATH

	val tokenFileContents: String =
		try {
			tokenFilePath.readText()
		} catch (_: NoSuchFileException) {
			System.err.println("${processInformation.stderrLoggingTag}: $tokenFilePath: no such file")
			exitProcess(NonExistingFile)
		}

	val token: BotToken? = BotToken.ofString(tokenFileContents.trim())

	if (token == null) {
		System.err.println("${processInformation.stderrLoggingTag}: $tokenFilePath: file contains invalid token")
		exitProcess(InvalidToken)
	}

	return token
}

private fun handleLocalCommandsConfigParsingFailure(
	processInformation: ProcessInformation,
	configFilePath: Path,
	failure: LocalCommandsConfigParsingResult.Failure,
): Nothing {
	val (message: String, exitStatus: ExitStatus) =
		when (failure) {
			is LocalCommandsConfigParsingResult.Failure.InvalidLine -> {
				"${failure.lineNumber}: invalid line" to LocalCommandsConfigInvalidLine
			}

			is LocalCommandsConfigParsingResult.Failure.InvalidCommandName -> {
				val message: String = "${failure.lineNumber}:${failure.columnNumber}: " +
					"${failure.invalidCommandName}: invalid command name"

				message to LocalCommandsConfigInvalidCommandName
			}

			is LocalCommandsConfigParsingResult.Failure.EmptyCommandLine -> {
				val message = "${failure.lineNumber}:${failure.columnNumber}: empty command line"
				message to LocalCommandsConfigEmptyCommandLine
			}

			is LocalCommandsConfigParsingResult.Failure.DuplicateCommand -> {
				val message = "${failure.lineNumber}: ${failure.commandName}: duplicate command"
				message to LocalCommandsConfigDuplicateCommand
			}

			is LocalCommandsConfigParsingResult.Failure.ZeroCommandsDefined -> {
				"zero commands defined" to LocalCommandsConfigZeroCommandsDefined
			}
		}

	System.err.println("${processInformation.stderrLoggingTag}: $configFilePath:$message")
	exitProcess(exitStatus)
}
