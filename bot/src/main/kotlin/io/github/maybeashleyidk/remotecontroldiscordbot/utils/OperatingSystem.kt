package io.github.maybeashleyidk.remotecontroldiscordbot.utils

internal enum class OperatingSystem {
	UnixLike,
	MicrosoftWindows,
	;

	companion object {

		fun determine(): OperatingSystem? {
			return System.getProperty("os.name")
				?.ifEmpty { null }
				?.let(this::determineFromOsNameString)
		}

		private fun determineFromOsNameString(string: String): OperatingSystem? {
			return when {
				OsNameString.isUnixLike(string) -> UnixLike
				OsNameString.isMicrosoftWindows(string) -> MicrosoftWindows
				else -> null
			}
		}
	}
}

private object OsNameString {

	private val unixLikeSubstrings: Set<String> = hashSetOf("linux", "mac", "bsd")

	fun isUnixLike(string: String): Boolean {
		return unixLikeSubstrings
			.any { unixLikeSubstring: String ->
				string.contains(unixLikeSubstring, ignoreCase = true)
			}
	}

	fun isMicrosoftWindows(string: String): Boolean {
		return string.contains("windows", ignoreCase = true)
	}
}
