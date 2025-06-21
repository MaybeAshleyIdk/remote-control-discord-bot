package io.github.maybeashleyidk.remotecontroldiscordbot.args

internal const val INSTANCE_NAME_ARGUMENT_PREFIX: String = "instance_name="

internal fun createProgramUsage(argv0: String): String {
	return "usage: $argv0 [$INSTANCE_NAME_ARGUMENT_PREFIX<name>]"
}
