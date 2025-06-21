package io.github.maybeashleyidk.remotecontroldiscordbot.localcommands

import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult.Failure.DuplicateCommand
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult.Failure.EmptyCommandLine
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult.Failure.InvalidCommandName
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult.Failure.InvalidLine
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfigParsingResult.Failure.ZeroCommandsDefined
import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.collections.immutable.toPersistentHashSet
import net.dv8tion.jda.api.utils.MiscUtil as JdaMiscUtils

private val AUTHORIZE_LINE_REGEX: Regex = Regex("""^\s*authorize\s+(?<userId>[1-9][0-9]*)\s*$""")
private val COMMAND_LINE_REGEX: Regex = Regex("""^\s*command\s+(?<commandName>[^:]+):\s*(?<commandLine>\S+.*)?$""")

public sealed class LocalCommandsConfigParsingResult {

	public data class Success(public val config: LocalCommandsConfig) : LocalCommandsConfigParsingResult()

	public sealed class Failure : LocalCommandsConfigParsingResult() {

		public data class InvalidLine(public val lineNumber: Int) : Failure()

		public data class InvalidCommandName(
			public val lineNumber: Int,
			public val columnNumber: Int,
			public val invalidCommandName: String,
		) : Failure()

		public data class EmptyCommandLine(
			public val lineNumber: Int,
			public val columnNumber: Int,
		) : Failure()

		public data class DuplicateCommand(
			public val lineNumber: Int,
			public val commandName: LocalCommandName,
		) : Failure()

		public data object ZeroCommandsDefined : Failure()
	}
}

public fun String.parseToLocalCommandsConfig(): LocalCommandsConfigParsingResult {
	val lines: Sequence<IndexedValue<String>> = this.lineSequence()
		.withIndex()
		.map { (index: Int, line: String) ->
			IndexedValue(
				index = index + 1,
				value = line.substringBefore('#').trim(),
			)
		}
		.filter { (_: Int, line: String) ->
			line.isNotEmpty()
		}

	val authorizedUserIds: MutableSet<Long> = hashSetOf()
	val commands: MutableMap<LocalCommandName, LocalCommandDetails> = mutableMapOf()

	for ((lineNumber: Int, line: String) in lines) {
		if ('\u0000' in line) {
			return InvalidLine(lineNumber)
		}

		val authorizeMatch: MatchResult? = AUTHORIZE_LINE_REGEX.matchEntire(line)
		val commandMatch: MatchResult? = COMMAND_LINE_REGEX.matchEntire(line)

		if (authorizeMatch != null) {
			if (commandMatch != null) {
				// Shouldn't be possible.
				return InvalidLine(lineNumber)
			}

			val userId: Long = JdaMiscUtils.parseSnowflake(authorizeMatch.groups["userId"]!!.value)
			authorizedUserIds += userId
			continue
		}

		commandMatch ?: return InvalidLine(lineNumber)

		val commandNameGroup: MatchGroup = commandMatch.groups["commandName"]!!

		val commandName: LocalCommandName = LocalCommandName.ofString(commandNameGroup.value)
			?: return InvalidCommandName(
				lineNumber = lineNumber,
				columnNumber = commandNameGroup.range.first + 1,
				invalidCommandName = commandNameGroup.value,
			)

		val commandLineGroup: MatchGroup = commandMatch.groups["commandLine"]
			?: return EmptyCommandLine(
				lineNumber = lineNumber,
				columnNumber = commandNameGroup.range.last + 2,
			)
		val fullCommandLine: String = commandLineGroup.value.trimEnd()

		val stderrIgnoredMatch: MatchResult? = Regex("""^(\S+.*)\s+2>\s*/dev/null\s*$""").matchEntire(fullCommandLine)

		val isStderrIgnored: Boolean = (stderrIgnoredMatch != null)

		val commandLine: String =
			if (stderrIgnoredMatch != null) {
				stderrIgnoredMatch.groupValues[1].trimEnd()
			} else {
				fullCommandLine
			}
		val argv: ArgumentVector = commandLine
			.split(Regex("\\s+"))
			.toImmutableList()
			.let(::ArgumentVector)

		val old: LocalCommandDetails? = commands.putIfAbsent(commandName, LocalCommandDetails(argv, isStderrIgnored))
		if (old != null) {
			return DuplicateCommand(
				lineNumber = lineNumber,
				commandName = commandName,
			)
		}
	}

	if (commands.isEmpty()) {
		return ZeroCommandsDefined
	}

	val config =
		LocalCommandsConfig(
			authorizedUserIds = authorizedUserIds.toPersistentHashSet(),
			commandsMap = commands.toImmutableMap(),
		)
	return LocalCommandsConfigParsingResult.Success(config)
}
