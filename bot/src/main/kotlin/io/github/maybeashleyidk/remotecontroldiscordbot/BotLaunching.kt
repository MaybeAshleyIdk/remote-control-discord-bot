package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.internal.runBot
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

public fun CoroutineScope.launchBot(token: BotToken, localCommandsConfig: LocalCommandsConfig, logger: Logger) {
	launch {
		runBot(token, localCommandsConfig, logger)
	}
}
