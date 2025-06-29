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

include(":args-parsing-lib")

include(
	":logging:api",
	":logging:impl-stderr",
	":logging:slf4j",
)

include(
	":socket-ipc:api",
)

include(
	":local-commands",
	":bot",
	":main",
)
