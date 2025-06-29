package io.github.maybeashleyidk.remotecontroldiscordbot.argsparsinglib.options

public sealed class Option {

	public abstract val definition: OptionDefinition

	public data class WithoutValue(override val definition: OptionDefinition.WithoutArgument) : Option()

	public data class WithValue(
		override val definition: OptionDefinition.WithArgument,
		public val value: String,
	) : Option()
}
