package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.usage

import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands.OperandDefinition
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionDefinition
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.ImmutableSet

public data class VariantUsage(
	public val options: ImmutableSet<OptionDefinition>,
	public val rootOperand: OperandDefinition,
	public val branches: ImmutableMap<String, VariantBranch>,
) : Usage()
