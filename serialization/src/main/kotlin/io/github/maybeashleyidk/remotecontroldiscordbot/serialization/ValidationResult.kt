package io.github.maybeashleyidk.remotecontroldiscordbot.serialization

public sealed class ValidationResult<out T> {

	public data class Valid<out T>(val value: T) : ValidationResult<T>()

	public data class Invalid(val message: String) : ValidationResult<Nothing>()

	public inline fun <R> map(transform: (T) -> R): ValidationResult<R> {
		return when (this) {
			is Valid -> Valid(transform(this.value))
			is Invalid -> this
		}
	}

	public companion object {

		public inline fun <T : R, R> ValidationResult<T>.getOrElse(defaultValue: (invalidMessage: String) -> R): R {
			return when (this) {
				is Valid -> this.value
				is Invalid -> defaultValue(this.message)
			}
		}

		public fun <T> ValidationResult<T>.getOrNull(): T? {
			return this.getOrElse { null }
		}

		public fun <T> ValidationResult<T>.getOrThrow(): T {
			return this.getOrElse(::error)
		}

		public inline fun <T1, T2, R> merge(
			result1: ValidationResult<T1>,
			result2: ValidationResult<T2>,
			combine: (T1, T2) -> R,
		): ValidationResult<R> {
			return when (result1) {
				is Valid -> {
					when (result2) {
						is Valid -> Valid(combine(result1.value, result2.value))
						is Invalid -> result2
					}
				}

				is Invalid -> result1
			}
		}

		public inline fun <T, R> Deserializer<ValidationResult<T>>.mapValidValue(
			elementName: String,
			crossinline transform: (T) -> R,
		): Deserializer<ValidationResult<R>> {
			return this
				.map(elementName) { result: ValidationResult<T> ->
					result.map(transform)
				}
		}
	}
}
