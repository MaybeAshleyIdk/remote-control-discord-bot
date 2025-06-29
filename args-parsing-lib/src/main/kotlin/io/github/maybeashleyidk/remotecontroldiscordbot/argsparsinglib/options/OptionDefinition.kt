package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.LongOptionIdentifier.Companion.toLongOptionIdentifier
import io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options.ShortOptionIdentifier.Companion.toShortOptionIdentifier
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentSetOf

public sealed class OptionDefinition {

	public abstract val shortIdentifiers: ImmutableSet<ShortOptionIdentifier>
	public abstract val longIdentifiers: ImmutableSet<LongOptionIdentifier>
	public abstract val priority: OptionPriority
	public abstract val description: String

	public data class WithoutArgument(
		override val shortIdentifiers: ImmutableSet<ShortOptionIdentifier>,
		override val longIdentifiers: ImmutableSet<LongOptionIdentifier>,
		override val priority: OptionPriority,
		override val description: String,
	) : OptionDefinition() {

		init {
			require((shortIdentifiers.size + longIdentifiers.size) > 0) {
				"An option definition requires at least one identifier"
			}
		}
	}

	public data class WithArgument(
		override val shortIdentifiers: ImmutableSet<ShortOptionIdentifier>,
		override val longIdentifiers: ImmutableSet<LongOptionIdentifier>,
		public val argument: OptionArgumentDefinition,
		override val priority: OptionPriority,
		override val description: String,
	) : OptionDefinition() {

		init {
			require((shortIdentifiers.size + longIdentifiers.size) > 0) {
				"An option definition requires at least one identifier"
			}
		}
	}

	public companion object {

		public val Help: WithoutArgument =
			WithoutArgument(
				shortIdentifiers = persistentSetOf('h'.toShortOptionIdentifier()),
				longIdentifiers = persistentSetOf("help".toLongOptionIdentifier()),
				priority = OptionPriority.High,
				description = "Print out help summary and exit successfully.",
			)

		public val VersionAndLegalInformation: WithoutArgument =
			WithoutArgument(
				shortIdentifiers = persistentSetOf('V'.toShortOptionIdentifier()),
				longIdentifiers = persistentSetOf("version".toLongOptionIdentifier()),
				priority = OptionPriority.High,
				description = "Print out version and legal information and exit successfully.",
			)
	}
}
