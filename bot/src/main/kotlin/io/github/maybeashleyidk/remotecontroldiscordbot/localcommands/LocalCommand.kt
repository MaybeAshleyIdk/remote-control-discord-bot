package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector

internal data class LocalCommand(
	val name: LocalCommandName,
	private val details: LocalCommandDetails,
) {

	val argv: ArgumentVector by details::argv
	val isStderrIgnored: Boolean by details::isStderrIgnored
}
