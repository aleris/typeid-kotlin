package earth.adi.typeid

import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Predicate
import java.util.function.Supplier

/**
 * A validated value that can be either valid or invalid.
 *
 * @param TId the type of validated value that can be any type. Used for id of type [RawId] or [Id].
 */
sealed class Validated<TId> {
  /**
   * A valid TypeId.
   *
   * @param id the id.
   */
  data class Valid<TId>(val id: TId) : Validated<TId>()

  /**
   * An invalid TypeId with an [error] message.
   *
   * @param error the error message.
   */
  data class Invalid<TId>(val error: String) : Validated<TId>()

  /**
   * Returns the value if valid, otherwise return the result of the provided supplier.
   *
   * @param otherSupplier the supplier to be called if the value is invalid
   * @return the value if valid, else the result of the supplier
   */
  fun orElse(otherSupplier: Supplier<TId>): TId {
    return when (this) {
      is Valid -> id
      is Invalid -> otherSupplier.get()
    }
  }

  /**
   * Returns the value if valid, otherwise return `other`.
   *
   * @param other the value to be returned if the value is invalid
   * @return the value if valid, else `other`
   */
  fun orElse(other: TId): TId {
    return orElse { other }
  }

  /**
   * Applies the provided mapping function if the value is valid, otherwise returns the current
   * invalid instance
   *
   * @param mapper the mapping function
   * @param [O] The type of the mapping function's result
   * @return the result of applying the mapping function to the value of this [Validated] instance
   */
  fun <O> map(mapper: Function<in TId, out O>): Validated<O> {
    return when (this) {
      is Valid -> Valid(mapper.apply(id))
      is Invalid -> Invalid(error)
    }
  }

  /**
   * If the value is valid and matches the predicate, return it as a valid [Validated], otherwise
   * return an invalid [Validated] with the given error message. If the value is invalid in the
   * first place, return the current invalid [Validated]
   *
   * @param errorMessageSupplier the supplier of the error message in case the predicate doesn't
   *   match
   * @param predicate the predicate that checks the value
   * @return the resulting [Validated]
   */
  fun filter(errorMessageSupplier: Supplier<String>, predicate: Predicate<in TId>): Validated<TId> {
    return when (this) {
      is Valid -> if (predicate.test(id)) this else Invalid(errorMessageSupplier.get())
      is Invalid -> this
    }
  }

  /**
   * If the value is valid and matches the predicate, return it as a valid [Validated], otherwise
   * return an invalid [Validated] with the given error message. If the value is invalid in the
   * first place, return the current invalid [Validated]
   *
   * @param errorMessage the error message in case the predicate doesn't match
   * @param predicate the predicate that checks the value
   * @return the resulting [Validated]
   */
  fun filter(errorMessage: String, predicate: Predicate<in TId>): Validated<TId> {
    return filter({ errorMessage }, predicate)
  }

  /**
   * Applies the consuming function for the value if is valid, otherwise does nothing.
   *
   * @param valueConsumer the value [Consumer]
   */
  fun ifValid(valueConsumer: Consumer<TId>) {
    if (this is Valid) {
      valueConsumer.accept(id)
    }
  }

  /**
   * Applies the message consuming function if the value is invalid, otherwise does nothing.
   *
   * @param messageConsumer the message [Consumer]
   */
  fun ifInvalid(messageConsumer: Consumer<String>) {
    if (this is Invalid) {
      messageConsumer.accept(error)
    }
  }
}
