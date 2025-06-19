package io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr

import io.github.maybeashleyidk.remotecontroldiscordbot.logging.LogLevel
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

private val TimestampFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("u-MM-dd HH:mm:ss.SS")

public object StderrLogger : Logger {

	private object Lock

	override fun log(scopes: ImmutableList<String>, level: LogLevel, message: String) {
		val timestamp: LocalDateTime = LocalDateTime.now()

		val logString: String =
			prepareLogString(
				timestamp = timestamp,
				scopes = scopes,
				level = level,
				unpreparedMessage = message,
			)

		synchronized(Lock) {
			System.err.println(logString)
		}
	}
}

private fun prepareLogString(
	timestamp: LocalDateTime,
	scopes: ImmutableList<String>,
	level: LogLevel,
	unpreparedMessage: String,
): String {
	val prefix: String = prepareLogStringPrefix(timestamp, scopes, level)

	return prefix +
		unpreparedMessage.replace(
			oldValue = "\n",
			newValue = "\n" + " ".repeat(prefix.length),
		)
}

private fun prepareLogStringPrefix(timestamp: LocalDateTime, scopes: ImmutableList<String>, level: LogLevel): String {
	val levelStr: String =
		when (level) {
			LogLevel.Info -> "INFO"
			LogLevel.Warning -> "WARN"
		}

	return "[${TimestampFormatter.format(timestamp)}] " +
		"$levelStr: " +
		scopes
			.map(String::trim)
			.filter(String::isNotEmpty)
			.joinToString(separator = "/")
			.ifNotEmpty { "$it: " }
}

private inline fun String.ifNotEmpty(transform: (String) -> String): String {
	return if (this.isNotEmpty()) {
		transform(this)
	} else {
		this
	}
}
