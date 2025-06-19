package io.github.maybeashleyidk.remotecontroldiscordbot.logging.slf4j

import io.github.maybeashleyidk.remotecontroldiscordbot.logging.stderr.StderrLogger

internal class CustomSlf4jServiceProvider : org.slf4j.spi.SLF4JServiceProvider {

	// <https://github.com/qos-ch/slf4j/blob/v_2.0.17/slf4j-simple/src/main/java/org/slf4j/simple/SimpleServiceProvider.java>

	companion object {

		/**
		 * Declare the version of the SLF4J API this implementation is compiled against.
		 * The value of this field is modified with each major release.
		 */
		// to avoid constant folding by the compiler, this field must *not* be final
		@JvmField
		var REQUESTED_API_VERSION: String = "2.0.99"
	}

	private lateinit var loggerFactory: org.slf4j.ILoggerFactory
	private val markerFactory: org.slf4j.IMarkerFactory = org.slf4j.helpers.BasicMarkerFactory()
	private val mdcAdapter: org.slf4j.spi.MDCAdapter = org.slf4j.helpers.NOPMDCAdapter()

	override fun getLoggerFactory(): org.slf4j.ILoggerFactory {
		return this.loggerFactory
	}

	override fun getMarkerFactory(): org.slf4j.IMarkerFactory {
		return this.markerFactory
	}

	override fun getMDCAdapter(): org.slf4j.spi.MDCAdapter {
		return this.mdcAdapter
	}

	override fun getRequestedApiVersion(): String {
		return REQUESTED_API_VERSION
	}

	override fun initialize() {
		this.loggerFactory = CustomSlf4jLoggerFactory(logger = StderrLogger)
	}
}
