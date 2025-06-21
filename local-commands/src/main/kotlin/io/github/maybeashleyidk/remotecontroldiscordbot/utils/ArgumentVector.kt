package io.github.maybeashleyidk.remotecontroldiscordbot.utils

import kotlinx.collections.immutable.ImmutableList

@JvmInline
public value class ArgumentVector(private val list: ImmutableList<String>) {

	init {
		require(list.isNotEmpty()) { "An argument vector must not be empty" }
		require(list[0].isNotEmpty()) { "An argument vector's first element (the executable name) must not be empty" }
		require(list.all { '\u0000' !in it }) { "The argument vector's elements must not contain an ASCII NUL character" }
	}

	public fun toCommandLine(): String {
		return this.list.joinToString(separator = " ", transform = String::quotedIfNecessary)
	}

	public fun toArray(): Array<String> {
		return this.list.toTypedArray()
	}

	override fun toString(): String {
		return this.list.toString()
	}
}

private fun String.quotedIfNecessary(): String {
	if (this.isEmpty()) {
		return "\"\""
	}

	if (this.all(Char::doesNotRequireQuoting)) {
		return this
	}

	return this
		.replace("\\", "\\\\")
		.replace("\"", "\\\"")
}

private fun Char.doesNotRequireQuoting(): Boolean {
	return (this in 'a'..'z') ||
		(this in 'A'..'Z') ||
		(this in '0'..'9') ||
		(this in '+'..'/') // also includes the comma (','), hyphen-minus ('-') and full stop ('.')
}
