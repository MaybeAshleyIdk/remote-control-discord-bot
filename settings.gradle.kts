rootProject.name = "remote-control-discord-bot"

pluginManagement {
	repositories {
		gradlePluginPortal()
	}
}

dependencyResolutionManagement {
	repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
	repositories {
		mavenCentral()
	}
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

include(
	":logging:api",
	":logging:impl-stderr",
	":logging:slf4j",
	":bot",
)
