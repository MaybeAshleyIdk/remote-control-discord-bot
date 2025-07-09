package io.github.maybeashleyidk.remotecontroldiscordbot

import kotlin.system.exitProcess

internal enum class ExitStatus(val code: Int) {
	ExcessiveArguments(4),
	EmptyArgument(9),
	ArgumentWithoutPrefix(13),
	EmptyInstanceName(14),
	InvalidInstanceName(15),
	NonExistingFile(24),
	HomeEnvironmentVariableUnsetOrEmpty(48),
	HomeEnvironmentVariableNotAbsolute(49),
	XdgRuntimeDirEnvironmentVariableUnsetOrEmpty(50),
	XdgRuntimeDirEnvironmentVariableNotAbsolute(51),
	LocalCommandsConfigInvalidLine(52),
	LocalCommandsConfigInvalidCommandName(53),
	LocalCommandsConfigEmptyCommandLine(54),
	LocalCommandsConfigDuplicateCommand(55),
	LocalCommandsConfigZeroCommandsDefined(56),
	InvalidToken(57),
	IpcSocketAlreadyExists(58),
}

internal fun exitProcess(status: ExitStatus): Nothing {
	exitProcess(status = status.code)
}
