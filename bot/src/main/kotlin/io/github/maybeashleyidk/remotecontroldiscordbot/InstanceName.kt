package io.github.maybeashleyidk.remotecontroldiscordbot

@JvmInline
internal value class InstanceName private constructor(private val nameString: String) {

	override fun toString(): String {
		return this.nameString
	}

	companion object {

		val DEFAULT: InstanceName = InstanceName("default")

		fun ofString(nameString: String): InstanceName? {
			if (!(nameString.isInstanceName())) {
				return null
			}

			return InstanceName(nameString)
		}
	}
}

private fun String.isInstanceName(): Boolean {
	return this.split('_', '-')
		.ifEmpty { return@isInstanceName false }
		.all { substring: String ->
			substring.isNotEmpty() && substring.all(Char::isLatinLetterOrAsciiDigit)
		}
}

private fun Char.isLatinLetterOrAsciiDigit(): Boolean {
	return (this in 'a'..'z') || (this in 'A'..'Z') || (this in '0'..'9')
}
