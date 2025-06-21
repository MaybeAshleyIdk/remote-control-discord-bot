package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector

public data class LocalCommandDetails(
	public val argv: ArgumentVector,
	public val isStderrIgnored: Boolean,
)
