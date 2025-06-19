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
import io.github.maybeashleyidk.remotecontroldiscordbot.env.getInstanceConfigDirectoryPathOrExit
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.parseToLocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr.StderrLogger
import java.nio.file.NoSuchFileException
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.readText

private const val INSTANCE_NAME_ARGUMENT_PREFIX: String = "instance_name="

private val LOCAL_COMMANDS_CONFIG_FILE_RELATIVE_PATH: Path = Path.of("commands.cfg")
private val TOKEN_FILE_RELATIVE_PATH: Path = Path.of("token.txt")

class Main {

	companion object {

		@JvmStatic
		fun main(args: Array<String>) {
			main(processInformation = ProcessInformation.determine(mainClass = Main::class.java), args = args)
		}
	}
}

private fun main(processInformation: ProcessInformation, args: Array<String>) {
	main(processInformation, instanceName = getInstanceNameOrExit(processInformation, args))
}

private fun getInstanceNameOrExit(processInformation: ProcessInformation, args: Array<String>): InstanceName {
	val usage = "usage: ${processInformation.usageExecutableArgumentsString} [$INSTANCE_NAME_ARGUMENT_PREFIX<name>]"

	return when (args.size) {
		0 -> InstanceName.DEFAULT

		1 -> {
			val arg: String = args[0]

			if (arg.isEmpty()) {
				System.err.println("${processInformation.stderrLoggingTag}: argument must not be empty\n$usage")
				exitProcess(EmptyArgument)
			}

			val instanceNameStr: String = arg.removePrefix(INSTANCE_NAME_ARGUMENT_PREFIX)

			if (instanceNameStr == arg) {
				val message: String = "${processInformation.stderrLoggingTag}: $arg: must have " +
					"the prefix \"$INSTANCE_NAME_ARGUMENT_PREFIX\"\n$usage"
				System.err.println(message)

				exitProcess(ArgumentWithoutPrefix)
			}

			if (instanceNameStr.isEmpty()) {
				val message = "${processInformation.stderrLoggingTag}: instance name must not be empty\n$usage"
				System.err.println(message)

				exitProcess(InstanceNameEmpty)
			}

			val instanceName: InstanceName? = InstanceName.ofString(instanceNameStr)

			if (instanceName == null) {
				val message = "${processInformation.stderrLoggingTag}: $instanceNameStr: invalid instance name"
				System.err.println(message)

				exitProcess(InvalidInstanceName)
			}

			instanceName
		}

		else -> {
			System.err.println("${processInformation.stderrLoggingTag}: too many arguments: ${args.size}\n$usage")
			exitProcess(ExcessiveArguments)
		}
	}
}

private fun main(processInformation: ProcessInformation, instanceName: InstanceName) {
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
