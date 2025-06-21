package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.requests.RestAction
import java.util.concurrent.CompletableFuture

internal suspend fun <T> RestAction<T>.await(): T {
	val future: CompletableFuture<T> = this.submit()
	return future.await()
}
