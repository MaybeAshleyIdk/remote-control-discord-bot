plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	api(libs.kotlinx.immutableCollections)
}
