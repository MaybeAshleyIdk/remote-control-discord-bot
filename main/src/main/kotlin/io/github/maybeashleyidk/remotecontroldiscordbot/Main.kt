package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.ArgumentWithoutPrefix
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.EmptyArgument
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.ExcessiveArguments
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InstanceNameEmpty
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InvalidInstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InvalidToken
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigDuplicateCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigEmptyCommandLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidCommandName
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigInvalidLine
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.LocalCommandsConfigZeroCommandsDefined
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.NonExistingFile
import io.github.maybeashleyidk.remotecontroldiscordbot.args.INSTANCE_NAME_ARGUMENT_PREFIX
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArguments
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArgumentsParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.args.createProgramUsage
import io.github.maybeashleyidk.remotecontroldiscordbot.args.parseProgramArguments
import io.github.maybeashleyidk.remotecontroldiscordbot.env.getInstanceConfigDirectoryPathOrExit
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.parseToLocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr.StderrLogger
import kotlinx.collections.immutable.toImmutableList
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText

private val LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH: Path = Path.of("commands.cfg")
private val TOKEN_FILE_RELATIVE_PATH: Path = Path.of("token.txt")

class Main {

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			val processInformation: ProcessInformation = ProcessInformation.determine(mainClass = Main::class.java)

			val parsedArguments: ProgramArguments =
				when (val result: ProgramArgumentsParsingResult = parseProgramArguments(args.toImmutableList())) {
					is ProgramArgumentsParsingResult.Success -> result.arguments
					is ProgramArgumentsParsingResult.Failure -> {
						handleProgramArgumentParsingFailure(processInformation, failure = result)
					}
				}

			main(processInformation = processInformation, arguments = parsedArguments)
		}
	}
}

private fun handleProgramArgumentParsingFailure(
	processInformation: ProcessInformation,
	failure: ProgramArgumentsParsingResult.Failure,
): Nothing {
	data class ExitInformation(
		val message: String,
		val usageIncluded: Boolean,
		val exitStatus: ExitStatus,
	)

	val information: ExitInformation =
		when (failure) {
			is ProgramArgumentsParsingResult.EmptyArgument -> {
				ExitInformation(
					message = "argument must not be empty",
					usageIncluded = true,
					exitStatus = EmptyArgument,
				)
			}

			is ProgramArgumentsParsingResult.ArgumentWithoutPrefix -> {
				ExitInformation(
					message = "${failure.argument}: must have the prefix \"$INSTANCE_NAME_ARGUMENT_PREFIX\"",
					usageIncluded = true,
					exitStatus = ArgumentWithoutPrefix,
				)
			}

			is ProgramArgumentsParsingResult.EmptyInstanceName -> {
				ExitInformation(
					message = "instance name must not be empty",
					usageIncluded = true,
					exitStatus = InstanceNameEmpty,
				)
			}

			is ProgramArgumentsParsingResult.InvalidInstanceName -> {
				ExitInformation(
					message = "${failure.instanceNameString}: invalid instance name",
					usageIncluded = false,
					exitStatus = InvalidInstanceName,
				)
			}

			is ProgramArgumentsParsingResult.ExcessiveArguments -> {
				ExitInformation(
					message = "too many arguments: ${failure.count}",
					usageIncluded = true,
					exitStatus = ExcessiveArguments,
				)
			}
		}

	val messageWithUsage: String = processInformation.stderrLoggingTag + ": " + information.message +
		if (information.usageIncluded) {
			"\n" + createProgramUsage(argv0 = processInformation.usageExecutableArgumentsString)
		} else {
			""
		}
	System.err.println(messageWithUsage)

	exitProcess(information.exitStatus)
}

private fun main(processInformation: ProcessInformation, arguments: ProgramArguments) {
	val instanceName: InstanceName = arguments.instanceName ?: InstanceName.DEFAULT
	val instanceConfigDirectoryPath: Path = getInstanceConfigDirectoryPathOrExit(processInformation, instanceName)

	val localCommandsConfig: LocalCommandsConfig =
		getLocalCommandsConfigOrExit(processInformation, instanceConfigDirectoryPath)

	val token: BotToken = getTokenOrExit(processInformation, instanceConfigDirectoryPath)

	runBot(token, localCommandsConfig, logger = StderrLogger)
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
