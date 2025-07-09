package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.BotPauseReason
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.StringDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.StringSerializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map

private const val ELEMENT_NAME: String = "optional bot pause reason"

internal val OptionalBotPauseReasonSerializer: Serializer<BotPauseReason?> =
	Serializer(elementName = ELEMENT_NAME) { reason: BotPauseReason?, sink: Sink ->
		StringSerializer.write(reason?.toString().orEmpty(), sink)
	}

internal val OptionalBotPauseReasonDeserializer: Deserializer<ValidationResult<BotPauseReason?>> = StringDeserializer
	.map(elementName = ELEMENT_NAME) { reasonString: String ->
		if (reasonString.isEmpty()) {
			return@map ValidationResult.Valid(null)
		}

		val reason: BotPauseReason? = BotPauseReason.ofString(reasonString)

		if (reason != null) {
			ValidationResult.Valid(reason)
		} else {
			ValidationResult.Invalid(message = "Invalid bot pause reason string \"$reasonString\"")
		}
	}
