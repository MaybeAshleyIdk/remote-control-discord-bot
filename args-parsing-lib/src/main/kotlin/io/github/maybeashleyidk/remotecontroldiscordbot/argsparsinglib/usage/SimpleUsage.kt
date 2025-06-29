package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.usage

import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands.OperandDefinition
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionDefinition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf

public data class SimpleUsage(
	public val options: ImmutableSet<OptionDefinition>,
	public val operands: ImmutableList<Operand>,
) : Usage() {

	public data class Operand(
		val definition: OperandDefinition,
		val required: Boolean,
	)

	public companion object {

		public val Empty: SimpleUsage = SimpleUsage(options = persistentSetOf(), operands = persistentListOf())
	}
}
