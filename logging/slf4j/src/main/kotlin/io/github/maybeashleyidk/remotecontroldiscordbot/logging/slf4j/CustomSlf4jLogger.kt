package io.github.maybeashleyidk.remotecontroldiscordbot.logging.slf4j

import io.github.maybeashleyidk.remotecontroldiscordbot.logging.LogLevel
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger.Companion.log
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.withScopes
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

internal class CustomSlf4jLogger(logger: Logger, name: String?) : org.slf4j.helpers.LegacyAbstractLogger() {

	init {
		this.name = name
	}

	private val logger: Logger by lazy {
		val scopes: ImmutableList<String> =
			kotlin.run {
				val name: String = this.getName().orEmpty()

				val clazz: Class<*>? =
					try {
						Class.forName(name)
					} catch (_: ClassNotFoundException) {
						null
					}

				if (clazz != null) {
					val needsJdaScope: Boolean = clazz.name.startsWith("net.dv8tion.jda.") &&
						!(clazz.simpleName.equals("jda", ignoreCase = true))

					(if (needsJdaScope) persistentListOf("JDA") else persistentListOf()) +
						persistentListOf(clazz.simpleName)
				} else {
					persistentListOf(name.trim())
				}
			}

		logger.withScopes(scopes)
	}

	override fun getFullyQualifiedCallerName(): String? {
		return null
	}

	override fun handleNormalizedLoggingCall(
		level: org.slf4j.event.Level?,
		marker: org.slf4j.Marker?,
		messagePattern: String?,
		arguments: Array<out Any?>?,
		throwable: Throwable?,
	) {
		val logLevel: LogLevel = level?.toLogLevel() ?: return

		var message: String = org.slf4j.helpers.MessageFormatter.basicArrayFormat(messagePattern, arguments)
			.orEmpty().trim()

		val stackTraceString: String = throwable?.stackTraceToString()?.trim().orEmpty()
		if (stackTraceString.isNotEmpty()) {
			if (message.isNotEmpty() && !(message.startsWith(':'))) {
				message += ':'
			}

			message += " $stackTraceString"
		}

		this.logger.log(logLevel, message)
	}

	override fun isTraceEnabled(): Boolean {
		return this.isLevelEnabled(org.slf4j.event.Level.TRACE)
	}

	override fun isDebugEnabled(): Boolean {
		return this.isLevelEnabled(org.slf4j.event.Level.DEBUG)
	}

	override fun isInfoEnabled(): Boolean {
		return this.isLevelEnabled(org.slf4j.event.Level.INFO)
	}

	override fun isWarnEnabled(): Boolean {
		return this.isLevelEnabled(org.slf4j.event.Level.WARN)
	}

	override fun isErrorEnabled(): Boolean {
		return this.isLevelEnabled(org.slf4j.event.Level.ERROR)
	}

	private fun isLevelEnabled(level: org.slf4j.event.Level): Boolean {
		return (level.toLogLevel() != null)
	}
}

private fun org.slf4j.event.Level.toLogLevel(): LogLevel? {
	return when (this) {
		org.slf4j.event.Level.ERROR -> LogLevel.Warning
		org.slf4j.event.Level.WARN -> LogLevel.Warning
		org.slf4j.event.Level.INFO -> LogLevel.Info
		org.slf4j.event.Level.DEBUG -> null
		org.slf4j.event.Level.TRACE -> null
	}
}

private operator fun <E> ImmutableList<E>.plus(collection: Collection<E>): ImmutableList<E> {
	if (this is PersistentList) {
		return this.addAll(collection)
	}

	return ((this as List<E>) + collection).toImmutableList()
}
