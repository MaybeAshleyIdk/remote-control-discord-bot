package io.github.maybeashleyidk.remotecontroldiscordbot.internal.utils

import io.github.maybeashleyidk.remotecontroldiscordbot.utils.ArgumentVector
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.OutputStream

internal suspend fun exec(argv: ArgumentVector, stdout: OutputStream, stderr: OutputStream?): Int {
	return coroutineScope {
		val process: Process = Runtime.getRuntime().exec(argv.toArray())

		launch(Dispatchers.IO) {
			launch { process.inputStream.transferTo(stdout) }

			if (stderr != null) {
				launch { process.errorStream.transferTo(stderr) }
			}
		}

		process.waitFor()
	}
}
