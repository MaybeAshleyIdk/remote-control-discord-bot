package io.github.maybeashleyidk.remotecontroldiscordbot.args

import io.github.maybeashleyidk.remotecontroldiscordbot.InstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArgumentsParsingResult as Result
import kotlinx.collections.immutable.ImmutableList

internal fun parseProgramArguments(arguments: ImmutableList<String>): Result {
	return when (arguments.size) {
		0 -> Result.Success(arguments = ProgramArguments(instanceName = null, isBot = true))
		1 -> parseSingleArgument(argument = arguments[0])
		2 -> {
			val r = parseSingleArgument(argument = arguments[0])
			if (r is Result.Success) {
				check(arguments[1] == "ipc")
				r.copy(arguments = r.arguments.copy(isBot = false))
			} else {
				r
			}
		}

		else -> Result.ExcessiveArguments(count = arguments.size - 1)
	}
}

private fun parseSingleArgument(argument: String): Result {
	if (argument.isEmpty()) {
		return Result.EmptyArgument
	}

	val instanceNameString: String = argument.removePrefix(INSTANCE_NAME_ARGUMENT_PREFIX)

	if (instanceNameString == argument) {
		return Result.ArgumentWithoutPrefix(argument)
	}
	if (instanceNameString.isEmpty()) {
		return Result.EmptyInstanceName
	}

	val instanceName: InstanceName = InstanceName.ofString(instanceNameString)
		?: return Result.InvalidInstanceName(instanceNameString)

	val arguments = ProgramArguments(instanceName, isBot = true)
	return Result.Success(arguments)
}
