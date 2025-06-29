package io.github.maybeashleyidk.remotecontroldiscordbot.args

import io.github.maybeashleyidk.remotecontroldiscordbot.InstanceName
import io.github.maybeashleyidk.remotecontroldiscordbot.args.ProgramArgumentsParsingResult as Result
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands.OperandDefinition
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.operands.OperandName.Companion.toOperandName
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.LongOptionIdentifier.Companion.toLongOptionIdentifier
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionArgumentDefinition
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionArgumentName.Companion.toOptionArgumentName
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionDefinition
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.OptionPriority
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.ShortOptionIdentifier.Companion.toShortOptionIdentifier
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.usage.SimpleUsage
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.usage.VariantBranch
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.usage.VariantUsage
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentMapOf
import kotlinx.collections.immutable.persistentSetOf

//internal val InstanceNameOptionDefinition: OptionDefinition.WithArgument =
//	OptionDefinition(
//		shortIdentifiers = persistentSetOf('i'.toShortOptionIdentifier()),
//		longIdentifiers = persistentSetOf("instance-name".toLongOptionIdentifier()),
//		argument = OptionArgumentDefinition(name = "name".toOptionArgumentName()),
//		priority = OptionPriority.Low,
//		description = "Set the instance name of the bot.",
//	)
//
//internal val usage: VariantUsage =
//	VariantUsage(
//		options = persistentSetOf(
//			OptionDefinition.Help,
//			OptionDefinition.VersionAndLegalInformation,
//			InstanceNameOptionDefinition,
//		),
//		rootOperand = OperandDefinition(name = "command".toOperandName()),
//		branches = persistentMapOf(
//			"start" to VariantBranch(usage = SimpleUsage.Empty, description = "Starts the bot."),
//			"stop" to VariantBranch(usage = SimpleUsage.Empty, description = "Shuts down the bot."),
//			"ping" to VariantBranch(usage = SimpleUsage.Empty, description = "Checks if the bot is running."),
//			"pause" to VariantBranch(
//				usage = SimpleUsage(
//					options = persistentSetOf(),
//					operands = persistentListOf(
//						SimpleUsage.Operand(
//							definition = OperandDefinition(name = "reason".toOperandName()),
//							required = false,
//						),
//					),
//				),
//				description = "Pauses the bot with an optional reason.",
//			),
//			"resume" to VariantBranch(usage = SimpleUsage.Empty, description = "Resumes the bot."),
//		),
//	)

internal fun parseProgramArguments(arguments: ImmutableList<String>): Result {
	

	return when (arguments.size) {
		0 -> Result.Success(arguments = ProgramArguments(instanceName = null))
		1 -> parseSingleArgument(argument = arguments[0])
		else -> Result.ExcessiveArguments(count = arguments.size - 1)
	}
}

private fun parseSingleArgument(argument: String): Result {
	if (argument.isEmpty()) {
		return Result.EmptyArgument
	}

	val instanceNameString: String = argument.removePrefix(INSTANCE_NAME_ARGUMENT_PREFIX)

	if (instanceNameString == argument) {
		return Result.ArgumentWithoutPrefix(argument)
	}
	if (instanceNameString.isEmpty()) {
		return Result.EmptyInstanceName
	}

	val instanceName: InstanceName = InstanceName.ofString(instanceNameString)
		?: return Result.InvalidInstanceName(instanceNameString)

	val arguments = ProgramArguments(instanceName)
	return Result.Success(arguments)
}
