plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	api(libs.kotlinx.coroutinesCore)

	// Only required for the class `BotPauseReason`.
	// TODO: Move `BotPauseReason` to a separate module.
	api(projects.bot)
}
