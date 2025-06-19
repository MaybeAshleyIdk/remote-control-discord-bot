package io.github.maybeashleyidk.remotecontroldiscordbot.logging

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

public fun Logger.withScopes(scopes: ImmutableList<String>): Logger {
	return (this as? ScopedLogger)?.withScopes(scopes)
		?: ScopedLogger(baseLogger = this, baseScopes = scopes)
}

public fun Logger.withScopes(vararg scopes: String): Logger {
	return this.withScopes(scopes.toImmutableList())
}

public fun Logger.withScope(scope: String): Logger {
	return this.withScopes(persistentListOf(scope))
}

private data class ScopedLogger(
	private val baseLogger: Logger,
	private val baseScopes: ImmutableList<String>,
) : Logger {

	override fun log(scopes: ImmutableList<String>, level: LogLevel, message: String) {
		this.baseLogger.log(scopes = (this.baseScopes + scopes), level, message)
	}

	fun withScopes(scopes: ImmutableList<String>): Logger {
		return this.copy(baseScopes = this.baseScopes + scopes)
	}
}

private operator fun <E> ImmutableList<E>.plus(collection: Collection<E>): ImmutableList<E> {
	if (this is PersistentList) {
		return this.addAll(collection)
	}

	return ((this as List<E>) + collection).toImmutableList()
}
