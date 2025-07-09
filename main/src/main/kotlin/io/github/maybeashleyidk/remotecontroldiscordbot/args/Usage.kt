package io.github.maybeashleyidk.remotecontroldiscordbot.args

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

internal const val INSTANCE_NAME_ARGUMENT_PREFIX: String = "instance_name="

internal const val INSTANCE_NAME_OPTION_NAME: String = "instance-name"

private val USAGES: ImmutableList<String> =
	persistentListOf(
		"[--$INSTANCE_NAME_OPTION_NAME=<name>] start",
		"[--$INSTANCE_NAME_OPTION_NAME=<name>] ping",
		"[--$INSTANCE_NAME_OPTION_NAME=<name>] pause [<reason>]",
		"[--$INSTANCE_NAME_OPTION_NAME=<name>] resume",
		"[--$INSTANCE_NAME_OPTION_NAME=<name>] stop",
	)

internal fun createProgramUsage(argv0: String): String {
	return USAGES
		.asSequence()
		.map { "$argv0 $it" }
		.joinToString(prefix = "usage: ", separator = "\n   or: ")
}
