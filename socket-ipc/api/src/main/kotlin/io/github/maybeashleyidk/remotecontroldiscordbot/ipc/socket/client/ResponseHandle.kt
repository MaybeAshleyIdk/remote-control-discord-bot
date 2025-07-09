package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.client

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState

public sealed interface ResponseHandle {

	public interface Status : ResponseHandle {

		public suspend fun receiveCurrentState(): BotState
	}

	public interface Pause : ResponseHandle {

		public suspend fun receiveOldState(): BotState
	}

	public interface Resume : ResponseHandle {

		public suspend fun receiveOldState(): BotState
	}

	public interface Shutdown : ResponseHandle {

		public suspend fun receiveShutdownFinished()
	}
}
