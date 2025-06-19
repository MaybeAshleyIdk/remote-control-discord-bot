package io.github.maybeashleyidk.remotecontroldiscordbot.logging.slf4j

import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

internal class CustomSlf4jLoggerFactory(private val logger: Logger) : org.slf4j.ILoggerFactory {

	// <https://github.com/qos-ch/slf4j/blob/v_2.0.17/slf4j-simple/src/main/java/org/slf4j/simple/SimpleLoggerFactory.java>

	private val loggerMap: ConcurrentMap<String?, CustomSlf4jLogger> = ConcurrentHashMap()

	override fun getLogger(name: String?): CustomSlf4jLogger {
		return this.loggerMap.computeIfAbsent(name, this::createLogger)
	}

	private fun createLogger(name: String?): CustomSlf4jLogger {
		return CustomSlf4jLogger(logger = this.logger, name = name)
	}
}
