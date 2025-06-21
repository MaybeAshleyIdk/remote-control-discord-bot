plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	api(libs.kotlinx.immutableCollections)

	implementation(libs.jda) {
		exclude(module = "opus-java")
	}
}
