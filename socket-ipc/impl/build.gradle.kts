plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	implementation(libs.kotlinx.coroutinesCore)

	api(projects.socketIpc.api)
	implementation(projects.serialization)
}
