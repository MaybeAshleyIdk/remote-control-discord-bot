package io.github.maybeashleyidk.remotecontroldiscordbot

import kotlin.system.exitProcess

internal enum class ExitStatus(val code: Int) {
	ExcessiveArguments(4),
	EmptyArgument(9),
	ArgumentWithoutPrefix(13),
	InstanceNameEmpty(14),
	InvalidInstanceName(15),
	NonExistingFile(24),
	HomeEnvironmentVariableUnsetOrEmpty(48),
	HomeEnvironmentVariableNotAbsolute(49),
	LocalCommandsConfigInvalidLine(50),
	LocalCommandsConfigInvalidCommandName(51),
	LocalCommandsConfigEmptyCommandLine(52),
	LocalCommandsConfigDuplicateCommand(53),
	LocalCommandsConfigZeroCommandsDefined(54),
	InvalidToken(55),
}

internal fun exitProcess(status: ExitStatus): Nothing {
	exitProcess(status = status.code)
}
