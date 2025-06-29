package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.ArgumentWithoutPrefix
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.EmptyArgument
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.EmptyInstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.ExcessiveArguments
import io.github.maybeashleyidk.remotecontroldiscordbot.ExitStatus.InvalidInstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.args.INSTANCE_NAME_ARGUMENT_PREFIX
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArguments
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArgumentsParsingResult
import io.github.maybeashleyidk.remotecontroldiscordbot.args.createProgramUsage
import io.github.maybeashleyidk.remotecontroldiscordbot.args.parseProgramArguments
import kotlinx.collections.immutable.toImmutableList

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
					exitStatus = EmptyInstanceName,
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

	mainBot(processInformation, instanceName)
}
