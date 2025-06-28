import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	`kotlin-dsl`
}

java {
	targetCompatibility = JavaVersion.VERSION_21
	sourceCompatibility = targetCompatibility
}

kotlin {
	compilerOptions {
		jvmTarget = provider { java.targetCompatibility.toJvmTarget() }
	}

	explicitApi()
}

fun JavaVersion.toJvmTarget(): JvmTarget {
	return JvmTarget.fromTarget(this@toJvmTarget.toString())
}
