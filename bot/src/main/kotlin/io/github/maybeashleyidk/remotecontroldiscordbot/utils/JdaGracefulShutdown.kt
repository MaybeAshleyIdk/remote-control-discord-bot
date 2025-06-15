package io.github.maybeashleyidk.remotecontroldiscordbot.utils

import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.OnlineStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val GRACEFUL_SHUTDOWN_TIMEOUT_DURATION: Duration = 10.seconds

internal fun Jda.performGracefulShutdown() {
	this.presence.setPresence(OnlineStatus.OFFLINE, null, false)

	this.shutdown()

	if (this.awaitShutdown(GRACEFUL_SHUTDOWN_TIMEOUT_DURATION.toJavaDuration())) {
		return
	}

	this.shutdownNow()
	this.awaitShutdown()
}
