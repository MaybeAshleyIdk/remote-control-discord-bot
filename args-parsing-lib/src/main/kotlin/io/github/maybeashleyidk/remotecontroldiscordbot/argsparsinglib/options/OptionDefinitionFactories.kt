package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

import kotlinx.collections.immutable.ImmutableSet

@Suppress("FunctionName")
public fun OptionDefinition(
	shortIdentifiers: ImmutableSet<ShortOptionIdentifier>,
	longIdentifiers: ImmutableSet<LongOptionIdentifier>,
	priority: OptionPriority,
	description: String,
): OptionDefinition.WithoutArgument {
	return OptionDefinition.WithoutArgument(shortIdentifiers, longIdentifiers, priority, description)
}

@Suppress("FunctionName")
public fun OptionDefinition(
	shortIdentifiers: ImmutableSet<ShortOptionIdentifier>,
	longIdentifiers: ImmutableSet<LongOptionIdentifier>,
	argument: OptionArgumentDefinition,
	priority: OptionPriority,
	description: String,
): OptionDefinition.WithArgument {
	return OptionDefinition.WithArgument(shortIdentifiers, longIdentifiers, argument, priority, description)
}
