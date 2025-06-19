package io.github.maybeashleyidk.remotecontroldiscordbot.logging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

public interface Logger {

	public fun log(scopes: ImmutableList<String>, level: LogLevel, message: String)

	public companion object {

		public fun Logger.log(scope: String, level: LogLevel, message: String) {
			this@log.log(scopes = persistentListOf(scope), level, message)
		}

		public fun Logger.log(level: LogLevel, message: String) {
			this@log.log(scopes = persistentListOf(), level, message)
		}

		public fun Logger.logInfo(scope: String, message: String) {
			this@logInfo.log(scope, level = LogLevel.Info, message)
		}

		public fun Logger.logWarning(scope: String, message: String) {
			this@logWarning.log(scope, level = LogLevel.Warning, message)
		}

		public fun Logger.logInfo(message: String) {
			this@logInfo.log(level = LogLevel.Info, message)
		}

		public fun Logger.logWarning(message: String) {
			this@logWarning.log(level = LogLevel.Warning, message)
		}
	}
}
