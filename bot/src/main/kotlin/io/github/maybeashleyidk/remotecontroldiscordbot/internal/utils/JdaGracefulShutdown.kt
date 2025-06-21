package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logInfo
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.logWarning
import net.dv8tion.jda.api.JDA as Jda
import net.dv8tion.jda.api.OnlineStatus
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration

private val GRACEFUL_SHUTDOWN_TIMEOUT_DURATION: Duration = 10.seconds

internal fun Jda.performGracefulShutdown(logger: Logger) {
	logger.logInfo("Shutting down...")

	this.presence.setPresence(OnlineStatus.OFFLINE, null, false)

	this.shutdown()

	if (this.awaitShutdown(GRACEFUL_SHUTDOWN_TIMEOUT_DURATION.toJavaDuration())) {
		logger.logInfo("Shutdown successful")
		return
	}

	val message: String = "Graceful shutdown did not succeed after $GRACEFUL_SHUTDOWN_TIMEOUT_DURATION." +
		"Attempting a forceful shutdown..."
	logger.logWarning(message)

	this.shutdownNow()
	this.awaitShutdown()

	logger.logWarning("Forceful shutdown successful")
}
