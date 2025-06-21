package io.github.maybeashleyidk.remotecontroldiscordbot.internal

import net.dv8tion.jda.api.interactions.DiscordLocale
import java.text.NumberFormat

internal sealed interface UiStringKey {

	sealed interface WithoutArguments : UiStringKey
	sealed interface WithOneTextArgument : UiStringKey
	sealed interface WithOneIntegerArgument : UiStringKey

	data object SlashCommandDescription : WithOneTextArgument

	data object UnauthorizedUser : WithoutArguments

	object LocalCommandExecution {
		data object Success : WithoutArguments
		data object Failure : WithOneIntegerArgument
	}
}

internal interface UiStringResolver {

	fun resolve(locale: DiscordLocale, key: UiStringKey.WithoutArguments): String
	fun resolve(locale: DiscordLocale, key: UiStringKey.WithOneTextArgument, arg: String): String
	fun resolve(locale: DiscordLocale, key: UiStringKey.WithOneIntegerArgument, arg: Long): String

	class WithLocale private constructor(private val resolver: UiStringResolver, private val locale: DiscordLocale) {

		fun resolve(key: UiStringKey.WithoutArguments): String {
			return this.resolver.resolve(this.locale, key)
		}

		fun resolve(key: UiStringKey.WithOneTextArgument, arg: String): String {
			return this.resolver.resolve(this.locale, key, arg)
		}

		fun resolve(key: UiStringKey.WithOneIntegerArgument, arg: Long): String {
			return this.resolver.resolve(this.locale, key, arg)
		}

		companion object {

			fun WithLocale.resolve(key: UiStringKey.WithOneIntegerArgument, arg: Int): String {
				return this@resolve.resolve(key, arg.toLong())
			}

			fun UiStringResolver.withLocale(locale: DiscordLocale): WithLocale {
				return WithLocale(resolver = this@withLocale, locale)
			}
		}
	}

	companion object {

		val English: UiStringResolver = EnglishUiStringResolver
	}
}

private object EnglishUiStringResolver : UiStringResolver {

	private const val EN_SPACE: Char = '\u2002'

	override fun resolve(locale: DiscordLocale, key: UiStringKey.WithoutArguments): String {

		return when (key) {
			UiStringKey.UnauthorizedUser -> "You are not authorized to use this app"
			UiStringKey.LocalCommandExecution.Success -> "The command was executed successfully"
		}
	}

	override fun resolve(locale: DiscordLocale, key: UiStringKey.WithOneTextArgument, arg: String): String {
		return when (key) {
			UiStringKey.SlashCommandDescription -> "Executes the local command:${EN_SPACE}$arg"
		}
	}

	override fun resolve(locale: DiscordLocale, key: UiStringKey.WithOneIntegerArgument, arg: Long): String {
		val formattedArg: String = NumberFormat.getIntegerInstance(locale.english().toLocale()).format(arg)

		return when (key) {
			UiStringKey.LocalCommandExecution.Failure -> "The command exited with the status code `$formattedArg`"
		}
	}

	private fun DiscordLocale.english(): DiscordLocale {
		return when (this@english) {
			DiscordLocale.ENGLISH_UK, DiscordLocale.ENGLISH_US -> this@english
			else -> DiscordLocale.ENGLISH_US
		}
	}
}
