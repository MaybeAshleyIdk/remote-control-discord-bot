package io.github.maybeashleyidk.remotecontroldiscordbot.utils

import java.nio.channels.FileChannel
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Path
import java.nio.file.StandardOpenOption.CREATE_NEW
import java.nio.file.StandardOpenOption.DELETE_ON_CLOSE
import java.nio.file.StandardOpenOption.READ
import java.nio.file.StandardOpenOption.WRITE
import java.nio.file.attribute.PosixFilePermission.OWNER_READ
import java.nio.file.attribute.PosixFilePermission.OWNER_WRITE
import java.nio.file.attribute.PosixFilePermissions
import java.util.EnumSet.of as enumSetOf
import kotlin.io.path.createTempFile
import kotlin.io.path.deleteIfExists

internal class SecureTemporaryFile private constructor(
	private val path: Path,
	val channel: FileChannel,
) : AutoCloseable {

	override fun close() {
		var exception: Exception? = null

		try {
			this.channel.close()
		} catch (e: Exception) {
			exception = e
		}

		try {
			this.path.deleteIfExists()
		} catch (e: Exception) {
			if (exception != null) {
				exception.addSuppressed(e)
			} else {
				exception = e
			}
		}

		if (exception != null) {
			throw exception
		}
	}

	override fun toString(): String {
		val quotedPath: String = this.path.toString()
			.replace("\\", "\\\\")
			.replace("\"", "\\\"")

		return "SecureTemporaryFile($quotedPath)"
	}

	companion object {

		tailrec fun open(prefix: String? = null, suffix: String? = null): SecureTemporaryFile {
			// Taken from this SO answer adjusted: <https://stackoverflow.com/a/38718815/9581962>

			val path: Path = createTempFile(prefix = prefix, suffix = suffix)

			val deleted: Boolean = path.deleteIfExists()
			if (!deleted) {
				return open(prefix, suffix)
			}

			val channel: FileChannel? =
				try {
					FileChannel.open(
						path,
						enumSetOf(READ, WRITE, CREATE_NEW, DELETE_ON_CLOSE),
						PosixFilePermissions.asFileAttribute(enumSetOf(OWNER_READ, OWNER_WRITE)),
					)
				} catch (_: FileAlreadyExistsException) {
					null
				}

			if (channel == null) {
				return open(prefix, suffix)
			}

			return SecureTemporaryFile(path, channel)
		}
	}
}

internal inline fun <R> useSecureTemporaryFile(
	prefix: String? = null,
	suffix: String? = null,
	block: (channel: FileChannel) -> R,
): R {
	return SecureTemporaryFile.open(prefix = prefix, suffix = suffix)
		.use { secureTemporaryFile: SecureTemporaryFile ->
			block(secureTemporaryFile.channel)
		}
}
