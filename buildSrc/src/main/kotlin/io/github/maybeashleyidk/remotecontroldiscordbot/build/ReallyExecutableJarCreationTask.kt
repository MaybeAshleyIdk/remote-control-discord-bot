package io.github.maybeashleyidk.remotecontroldiscordbot.build

import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.InputStream
import java.io.OutputStream

// <https://skife.org/java/unix/2011/06/20/really_executable_jars.html>

@CacheableTask
public abstract class ReallyExecutableJarCreationTask : DefaultTask() {

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	public abstract val jarExecScriptFile: RegularFileProperty

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	public abstract val inputJarFile: RegularFileProperty

	@get:OutputFile
	public abstract val outputFile: RegularFileProperty

	@TaskAction
	internal fun createExecutableJar() {
		val jarExecScriptFile: File = this.jarExecScriptFile.asFile.get()
		val inputJarFile: File = this.inputJarFile.asFile.get()
		val outputFile: File = this.outputFile.asFile.get()

		outputFile.outputStream().buffered().use { outputStream: OutputStream ->
			jarExecScriptFile.inputStream().buffered().use { jarExecScriptStream: InputStream ->
				jarExecScriptStream.transferTo(outputStream)
			}

			inputJarFile.inputStream().buffered().use { inputJarStream: InputStream ->
				inputJarStream.transferTo(outputStream)
			}
		}

		outputFile.setExecutable(true)
	}
}
