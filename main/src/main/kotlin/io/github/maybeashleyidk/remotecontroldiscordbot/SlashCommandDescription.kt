package io.github.maybeashleyidk.remotecontroldiscordbot

import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.commands.build.CommandData.MAX_DESCRIPTION_LENGTH

private const val ELLIPSIS: Char = '…'

internal fun createEnGbSlashCommandDescription(uiStringResolver: UiStringResolver, commandLine: String): String {
	return createEnglishSlashCommandDescription(uiStringResolver, DiscordLocale.ENGLISH_UK, commandLine)
}

internal fun createEnUsSlashCommandDescription(uiStringResolver: UiStringResolver, commandLine: String): String {
	return createEnglishSlashCommandDescription(uiStringResolver, DiscordLocale.ENGLISH_US, commandLine)
}

private fun createEnglishSlashCommandDescription(
	uiStringResolver: UiStringResolver,
	locale: DiscordLocale,
	fullCommandLine: String,
): String {
	return createEnglishSlashCommandDescription(
		resolveString = { commandLine: String ->
			uiStringResolver.resolve(locale, UiStringKey.SlashCommandDescription, commandLine)
		},
		commandLine = fullCommandLine,
	)
}

private inline fun createEnglishSlashCommandDescription(
	crossinline resolveString: (String) -> String,
	commandLine: String,
): String {
	val fullDescription: String = resolveString(commandLine.toMonospaceSpan())
	val fullDescriptionLength: Long = fullDescription.codePoints().count()

	if (fullDescriptionLength <= MAX_DESCRIPTION_LENGTH) {
		return fullDescription
	}

	return commandLine.codePoints()
		.limit(MAX_DESCRIPTION_LENGTH - (fullDescriptionLength - MAX_DESCRIPTION_LENGTH))
		.toArray()
		.let { String(codePoints = it, offset = 0, length = it.size) }
		.plus(ELLIPSIS)
		.toMonospaceSpan()
		.let(resolveString)
}

private fun String.toMonospaceSpan(): String {
	if ('`' !in this) {
		return "`$this`"
	}

	if (("``" !in this) && !(this.endsWith('`'))) {
		return "``$this``"
	}

	// The official Discord client doesn't render the markdown of the slash command description in some places, so it's
	// not worth trying anything fancy at this point.
	return this
}
