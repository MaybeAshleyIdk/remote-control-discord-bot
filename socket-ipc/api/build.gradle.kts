plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	// Only required for the class `BotPauseReason`.
	// TODO: Move `BotPauseReason` to a separate module.
	api(projects.bot)
}
