import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension

plugins {
	alias(libs.plugins.kotlinJvm) apply false
	alias(libs.plugins.shadow) apply false
}

val javaCompatibilityVersion = JavaVersion.VERSION_21

// `libs` is not accessible in the subprojects block, so we need to save what we need in the block outside.

val junit = libs.junit

val kotlinJvmPluginIdProvider: Provider<String> = libs.plugins.kotlinJvm
	.map(PluginDependency::getPluginId)

subprojects {
	pluginManager.withPlugin("java-library") {
		extensions.configure<JavaPluginExtension>("java") {
			targetCompatibility = javaCompatibilityVersion
			sourceCompatibility = targetCompatibility
		}

		pluginManager.withPlugin(kotlinJvmPluginIdProvider.get()) {
			extensions.configure<KotlinJvmProjectExtension>("kotlin") {
				explicitApi()
			}
		}

		addJunitDependencies()
		useJunitPlatformForTests()
	}

	pluginManager.withPlugin("application") {
		extensions.configure<JavaPluginExtension>("java") {
			targetCompatibility = javaCompatibilityVersion
			sourceCompatibility = targetCompatibility
		}

		addJunitDependencies()
		useJunitPlatformForTests()
	}

	pluginManager.withPlugin(kotlinJvmPluginIdProvider.get()) {
		extensions.configure<KotlinJvmProjectExtension>("kotlin") {
			compilerOptions {
				jvmTarget = this@subprojects.provider {
					this@subprojects.extensions.getByName<JavaPluginExtension>("java")
						.targetCompatibility
						.toJvmTarget()
				}

				extraWarnings = true
			}
		}

		dependencies {
			add("testImplementation", kotlin("test"))
		}
	}
}

fun JavaVersion.toJvmTarget(): JvmTarget {
	return JvmTarget.fromTarget(this@toJvmTarget.toString())
}

fun Project.useJunitPlatformForTests() {
	tasks.named<Test>("test") {
		useJUnitPlatform()
	}
}

fun Project.addJunitDependencies() {
	dependencies {
		add("testImplementation", platform(junit.bom))
		add("testImplementation", junit.jupiter)
		add("testRuntimeOnly", junit.platformLauncher)
	}
}
