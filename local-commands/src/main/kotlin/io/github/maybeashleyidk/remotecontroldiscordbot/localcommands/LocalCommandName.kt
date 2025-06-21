package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import net.dv8tion.jda.api.interactions.commands.build.CommandData.MAX_NAME_LENGTH

@JvmInline
public value class LocalCommandName private constructor(private val nameString: String) {

	override fun toString(): String {
		return this.nameString
	}

	public companion object {

		public fun ofString(nameString: String): LocalCommandName? {
			if (!(nameString.isLocalCommandName())) {
				return null
			}

			return LocalCommandName(nameString)
		}
	}
}

private fun String.isLocalCommandName(): Boolean {
	return (this.length in 0..(MAX_NAME_LENGTH)) &&
		this.split('_', '-')
			.ifEmpty { return@isLocalCommandName false }
			.all { substring: String ->
				val firstChar: Char = substring.firstOrNull()
					?: return@all false

				firstChar.isLowercaseLatinLetter() &&
					substring.drop(1).all(Char::isLowercaseLatinLetterOrAsciiDigit)
			}
}

private fun Char.isLowercaseLatinLetterOrAsciiDigit(): Boolean {
	return this.isLowercaseLatinLetter() || this.isAsciiDigit()
}

private fun Char.isLowercaseLatinLetter(): Boolean {
	return (this in 'a'..'z')
}

private fun Char.isAsciiDigit(): Boolean {
	return (this in '0'..'9')
}
