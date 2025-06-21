package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentHashSetOf
import java.lang.Character.COMBINING_SPACING_MARK
import java.lang.Character.CONNECTOR_PUNCTUATION
import java.lang.Character.CURRENCY_SYMBOL
import java.lang.Character.DASH_PUNCTUATION
import java.lang.Character.DECIMAL_DIGIT_NUMBER
import java.lang.Character.ENCLOSING_MARK
import java.lang.Character.END_PUNCTUATION
import java.lang.Character.FINAL_QUOTE_PUNCTUATION
import java.lang.Character.INITIAL_QUOTE_PUNCTUATION
import java.lang.Character.LETTER_NUMBER
import java.lang.Character.LINE_SEPARATOR
import java.lang.Character.LOWERCASE_LETTER
import java.lang.Character.MATH_SYMBOL
import java.lang.Character.MODIFIER_LETTER
import java.lang.Character.MODIFIER_SYMBOL
import java.lang.Character.NON_SPACING_MARK
import java.lang.Character.OTHER_LETTER
import java.lang.Character.OTHER_NUMBER
import java.lang.Character.OTHER_PUNCTUATION
import java.lang.Character.OTHER_SYMBOL
import java.lang.Character.PARAGRAPH_SEPARATOR
import java.lang.Character.SPACE_SEPARATOR
import java.lang.Character.START_PUNCTUATION
import java.lang.Character.TITLECASE_LETTER
import java.lang.Character.UPPERCASE_LETTER
import java.nio.ByteBuffer
import java.nio.CharBuffer
import java.nio.charset.CodingErrorAction
import java.nio.charset.MalformedInputException

private val SAFE_PRINTABLE_CATEGORIES: ImmutableSet<Int> =
	persistentHashSetOf(
		UPPERCASE_LETTER.toInt(),
		LOWERCASE_LETTER.toInt(),
		TITLECASE_LETTER.toInt(),
		MODIFIER_LETTER.toInt(),
		OTHER_LETTER.toInt(),
		NON_SPACING_MARK.toInt(),
		COMBINING_SPACING_MARK.toInt(),
		ENCLOSING_MARK.toInt(),
		DECIMAL_DIGIT_NUMBER.toInt(),
		LETTER_NUMBER.toInt(),
		OTHER_NUMBER.toInt(),
		CONNECTOR_PUNCTUATION.toInt(),
		DASH_PUNCTUATION.toInt(),
		START_PUNCTUATION.toInt(),
		END_PUNCTUATION.toInt(),
		INITIAL_QUOTE_PUNCTUATION.toInt(),
		FINAL_QUOTE_PUNCTUATION.toInt(),
		OTHER_PUNCTUATION.toInt(),
		MATH_SYMBOL.toInt(),
		CURRENCY_SYMBOL.toInt(),
		MODIFIER_SYMBOL.toInt(),
		OTHER_SYMBOL.toInt(),
		SPACE_SEPARATOR.toInt(),
		LINE_SEPARATOR.toInt(),
		PARAGRAPH_SEPARATOR.toInt(),
	)

internal fun ByteArray.isUtf8SafePrintable(): Boolean {
	val charBuffer: CharBuffer =
		try {
			Charsets.UTF_8.newDecoder()
				.onMalformedInput(CodingErrorAction.REPORT)
				.decode(ByteBuffer.wrap(this))
		} catch (e: MalformedInputException) {
			return false
		}

	return charBuffer
		.codePoints()
		.allMatch(Int::isSafePrintable)
}

private fun Int.isSafePrintable(): Boolean {
	return (Character.getType(this) in SAFE_PRINTABLE_CATEGORIES) || Character.isWhitespace(this)
}
