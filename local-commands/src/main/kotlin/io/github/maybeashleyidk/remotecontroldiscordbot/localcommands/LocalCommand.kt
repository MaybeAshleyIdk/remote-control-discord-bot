package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector

public data class LocalCommand(
	public val name: LocalCommandName,
	private val details: LocalCommandDetails,
) {

	public val argv: ArgumentVector by details::argv
	public val isStderrIgnored: Boolean by details::isStderrIgnored
}
