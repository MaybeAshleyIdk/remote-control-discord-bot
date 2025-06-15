package io.github.maybeashleyidk.remotecontroldiscordbot.utils

import kotlinx.collections.immutable.ImmutableList

@JvmInline
internal value class ArgumentVector(private val list: ImmutableList<String>) {

	init {
		require(list.isNotEmpty()) { "An argument vector must not be empty" }
		require(list[0].isNotEmpty()) { "An argument vector's first element (the executable name) must not be empty" }
		require(list.all { '\u0000' !in it }) { "The argument vector's elements must not contain an ASCII NUL character" }
	}

	fun toCommandLine(): String {
		return this.list.joinToString(separator = " ")
	}

	fun toArray(): Array<String> {
		return this.list.toTypedArray()
	}

	override fun toString(): String {
		return this.list.toString()
	}
}
