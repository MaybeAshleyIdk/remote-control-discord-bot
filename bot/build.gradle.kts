import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import io.github.maybeashleyidk.remotecontroldiscordbot.build.GzipCompressingTask
import io.github.maybeashleyidk.remotecontroldiscordbot.build.ReallyExecutableJarCreationTask
import java.io.Writer
import java.nio.file.FileAlreadyExistsException
import java.nio.file.Files
import java.nio.file.StandardOpenOption
import java.nio.file.attribute.PosixFilePermission
import java.nio.file.attribute.PosixFilePermissions
import java.util.EnumSet

plugins {
	application
	alias(libs.plugins.kotlinJvm)
	alias(libs.plugins.shadow)
}

group = "io.github.maybeashleyidk"
version = "0.1.0-indev01"

application {
	applicationName = "remote-control-discord-bot"
	mainClass = "io.github.maybeashleyidk.remotecontroldiscordbot.Main"
}

dependencies {
	implementation(libs.kotlinx.coroutinesCore)
	implementation(libs.kotlinx.immutableCollections)
	implementation(libs.jda)
}

// These are not needed since shadow is used.
tasks.distTar { enabled = false }
tasks.distZip { enabled = false }
tasks.installDist { enabled = false }

val createExecutable: TaskProvider<ReallyExecutableJarCreationTask> by tasks.registering(
	ReallyExecutableJarCreationTask::class,
) {
	val shadowJar: ShadowJar = tasks.shadowJar.get()

	group = shadowJar.group
	description = "Creates the bot executable file"

	dependsOn(shadowJar)

	jarExecScriptFile = project.layout.projectDirectory.file("jar-exec-script.sh")

	inputJarFile = shadowJar.outputs.files.singleFile

	outputFile = project.layout.buildDirectory
		.dir("executables")
		.map { destDir: Directory ->
			destDir.file("${project.application.applicationName}-${project.version}")
		}
}
tasks.assemble {
	dependsOn(createExecutable)
}

val createGzippedExecutable: TaskProvider<GzipCompressingTask> by tasks.registering(GzipCompressingTask::class) {
	val createExecutable: ReallyExecutableJarCreationTask = createExecutable.get()

	group = createExecutable.group
	description = "Creates the gzipped bot executable file"

	inputFile = createExecutable.outputFile
	outputFile = project.layout.buildDirectory
		.dir("gzipped-executables")
		.zip(createExecutable.outputFile.locationOnly) { destDir: Directory, executableFile: RegularFile ->
			destDir.file(executableFile.asFile.name + ".gz")
		}
}
tasks.assemble {
	dependsOn(createGzippedExecutable)
}

val copyArtifactToProjectRoot: TaskProvider<Task> by tasks.registering {
	inputs.file(createGzippedExecutable.map(GzipCompressingTask::outputFile))

	outputs.file(rootProject.layout.projectDirectory.file("${project.application.applicationName}.gz"))

	doFirst {
		val inputFile: File = inputs.files.singleFile
		val outputFile: File = outputs.files.singleFile

		inputFile.copyTo(outputFile, overwrite = true)
	}
}
tasks.clean {
	delete(copyArtifactToProjectRoot)
}

tasks.run.configure {
	val instanceName = "dev-testing"

	val runDir: Directory = rootProject.layout.projectDirectory.dir("run")
	val baseConfigDir: Directory = runDir.dir("config")

	workingDir = runDir.asFile
	environment("XDG_CONFIG_HOME", baseConfigDir.toString())
	args("instance_name=$instanceName")

	doFirst {
		workingDir.mkdirs()

		runDir.file(".gitignore").asFile.writeText("*\n")

		val instanceConfigDir: Directory = baseConfigDir
			.dir("remote-control-discord-bot")
			.dir("instances")
			.dir(instanceName)

		val tokenFile: RegularFile = instanceConfigDir.file("token.txt")
		val localCommandsConfigFile: RegularFile = instanceConfigDir.file("commands.cfg")

		instanceConfigDir.asFile.mkdirs()

		val tokenFileCreated: Boolean =
			try {
				Files.createFile(
					tokenFile.asFile.toPath(),
					PosixFilePermissions.asFileAttribute(
						EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE),
					),
				)
				true
			} catch (_: FileAlreadyExistsException) {
				false
			}
		check(!tokenFileCreated) {
			"New empty token file created at '$tokenFile'. Insert the token of your bot to continue."
		}
		check(tokenFile.asFile.readText().isNotBlank()) {
			"Token file at '$tokenFile' does not contain a token. Insert the token of your bot to continue."
		}

		val localCommandsConfigFileCreated: Boolean =
			try {
				Files.newBufferedWriter(
					localCommandsConfigFile.asFile.toPath(),
					StandardOpenOption.CREATE_NEW,
				)
			} catch (_: FileAlreadyExistsException) {
				null
			}.use { writer: Writer? ->
				writer ?: return@use false

				val contents: String =
					"""
				authorize [insert your user snowflake ID here]

				command foo: echo foo
				command bar: echo bar 2> /dev/null
				command touch_foobar: touch foobar
				""".trimIndent()

				writer.write("$contents\n")

				true
			}
		check(!localCommandsConfigFileCreated) {
			"New default local commands config file created at '$localCommandsConfigFile'. " +
				"Insert your user snowflake ID to continue."
		}
	}
}
