package io.github.maybeashleyidk.remotecontroldiscordbot.serialization.bytechannel

import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult.Companion.getOrNull
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult.Companion.getOrThrow
import java.io.IOException
import java.nio.channels.ClosedChannelException

public sealed class ReadResult<out T> {

	public data class Success<T>(public val element: T) : ReadResult<T>()

	public data class Closed(public val exception: ClosedChannelException) : ReadResult<Nothing>()

	public data class Failure(
		public val elementName: String,
		public val error: IOException,
	) : ReadResult<Nothing>() {

		init {
			require(error !is ClosedChannelException)
		}
	}

	public data class Incomplete(
		public val elementName: String,
		public val expected: Int,
		public val actual: Int,
	) : ReadResult<Nothing>()

	public fun getOrNull(): T? {
		return when (this) {
			is Success -> this.element
			is Closed -> null
			is Failure -> null
			is Incomplete -> null
		}
	}

	public fun getOrThrow(): T {
		when (this) {
			is Success -> return this.element
			is Closed -> throw this.exception
			is Failure -> throw IOException("Failed to read a ${this.elementName}", this.error)
			is Incomplete -> {
				val message: String = "Failed to read a ${this.elementName}: " +
					"Expected to read ${this.expected} bytes, but actually only read ${this.actual} bytes "

				throw IOException(message)
			}
		}
	}

	public companion object {

		public fun <T> ReadResult<ValidationResult<T>>.getValidOrNull(): T? {
			return this.getOrNull()?.getOrNull()
		}

		public fun <T> ReadResult<ValidationResult<T>>.getValidOrThrow(): T {
			return this.getOrThrow().getOrThrow()
		}
	}
}
