package io.github.maybeashleyidk.remotecontroldiscordbot.args

internal sealed class ProgramArgumentsParsingResult {

	data class Success(val arguments: ProgramArguments) : ProgramArgumentsParsingResult()

	sealed class Failure : ProgramArgumentsParsingResult()

	data object EmptyArgument : Failure()

	data class ArgumentWithoutPrefix(val argument: String) : Failure() {
		init {
			require(!(argument.startsWith(INSTANCE_NAME_ARGUMENT_PREFIX)))
		}
	}

	data object EmptyInstanceName : Failure()

	data class InvalidInstanceName(val instanceNameString: String) : Failure() {
		init {
			require(instanceNameString.isNotEmpty())
		}
	}

	data class ExcessiveArguments(val count: Int) : Failure() {
		init {
			require(count > 0)
		}
	}
}
