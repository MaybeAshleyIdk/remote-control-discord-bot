package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.server

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState

public sealed interface RequestHandle {

	public interface Status : RequestHandle {

		public suspend fun sendCurrentState(currentState: BotState)
	}

	public interface Pause : RequestHandle {

		public suspend fun sendOldState(oldState: BotState)
	}

	public interface Resume : RequestHandle {

		public suspend fun sendOldState(oldState: BotState)
	}

	public interface Shutdown : RequestHandle {

		public suspend fun sendShutdownFinished()
	}
}
