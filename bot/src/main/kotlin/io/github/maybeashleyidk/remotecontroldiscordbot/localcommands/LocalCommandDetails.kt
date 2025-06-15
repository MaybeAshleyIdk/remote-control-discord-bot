package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector

internal data class LocalCommandDetails(
	val argv: ArgumentVector,
	val isStderrIgnored: Boolean,
)
