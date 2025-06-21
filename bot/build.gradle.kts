plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	api(projects.localCommands)
	api(projects.logging.api)

	implementation(libs.kotlinx.coroutinesCore)
	implementation(libs.jda) {
		exclude(module = "opus-java")
	}

	implementation(projects.logging.slf4j)
}
