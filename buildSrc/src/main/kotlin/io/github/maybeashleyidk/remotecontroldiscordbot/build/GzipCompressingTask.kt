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
import java.util.zip.GZIPOutputStream

@CacheableTask
public abstract class GzipCompressingTask : DefaultTask() {

	init {
		this.description = "Compresses a file using gzip"
	}

	@get:InputFile
	@get:PathSensitive(PathSensitivity.NONE)
	public abstract val inputFile: RegularFileProperty

	@get:OutputFile
	public abstract val outputFile: RegularFileProperty

	@TaskAction
	internal fun compressFile() {
		val inputFile: File = this.inputFile.asFile.get()
		val outputFile: File = this.outputFile.asFile.get()

		outputFile.outputStream().buffered().let(::GZIPOutputStream).use { outputStream: OutputStream ->
			inputFile.inputStream().buffered().use { inputStream: InputStream ->
				inputStream.transferTo(outputStream)
			}
		}
	}
}
