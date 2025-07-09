plugins {
	`java-library`
	alias(libs.plugins.kotlinJvm)
}

dependencies {
	implementation(libs.kotlinx.immutableCollections)
}
