plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	implementation(projects.logging.implStderr)
	implementation(libs.slf4jApi)
}
