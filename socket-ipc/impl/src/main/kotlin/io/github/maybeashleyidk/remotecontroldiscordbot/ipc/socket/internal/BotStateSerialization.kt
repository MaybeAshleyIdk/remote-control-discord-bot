package io.github.maybeashleyidk.remotecontroldiscordbot.ipc.socket.internal

import io.github.maybeashleyidk.remotecontroldiscordbot.BotState
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ByteDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Deserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.EmptyDeserializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Serializer
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.Sink
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.ValidationResult.Companion.mapValidValue
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.map
import io.github.maybeashleyidk.remotecontroldiscordbot.serialization.then

private const val TYPE_RESUMED: Byte = 1
private const val TYPE_PAUSED: Byte = 2

private const val ELEMENT_NAME: String = "bot state"

internal val BotStateSerializer: Serializer<BotState> =
	Serializer(elementName = ELEMENT_NAME) { state: BotState, sink: Sink ->
		when (state) {
			is BotState.Resumed -> sink.writeByte(TYPE_RESUMED)
			is BotState.Paused -> {
				sink.writeByte(TYPE_PAUSED)
				OptionalBotPauseReasonSerializer.write(state.reason, sink)
			}
		}
	}

internal val BotStateDeserializer: Deserializer<ValidationResult<BotState>> = ByteDeserializer
	.then(elementName = ELEMENT_NAME) { type: Byte ->
		when (type) {
			TYPE_RESUMED -> {
				EmptyDeserializer.map(elementName = "resumed $ELEMENT_NAME") {
					ValidationResult.Valid(BotState.Resumed)
				}
			}

			TYPE_PAUSED -> {
				OptionalBotPauseReasonDeserializer.mapValidValue(
					elementName = "paused $ELEMENT_NAME",
					transform = BotState::Paused,
				)
			}

			else -> {
				EmptyDeserializer.map(elementName = "invalid $ELEMENT_NAME") {
					ValidationResult.Invalid(message = "Invalid bot state type $type")
				}
			}
		}
	}
