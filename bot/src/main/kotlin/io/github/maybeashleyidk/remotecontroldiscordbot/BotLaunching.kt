package io.github.maybeashleyidk.remotecontroldiscordbot

import io.github.maybeashleyidk.remotecontroldiscordbot.internal.runBot
import io.github.maybeashleyidk.remotecontroldiscordbot.localcommands.LocalCommandsConfig
import io.github.maybeashleyidk.remotecontroldiscordbot.logging.Logger
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

public fun CoroutineScope.launchBot(
	token: BotToken,
	localCommandsConfig: LocalCommandsConfig,
	logger: Logger,
): Deferred<Bot> {
	val deferredStateReference: CompletableDeferred<BotStateReference> = CompletableDeferred()

	val botJob: Job =
		launch {
			runBot(token, localCommandsConfig, outState = deferredStateReference::complete, logger)
		}

	return async {
		val stateReference: BotStateReference = deferredStateReference.await()
		Bot(job = botJob, stateReference)
	}
}
