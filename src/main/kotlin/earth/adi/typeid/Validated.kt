package earth.adi.typeid

/**
 * A validated TypeId that can be either valid or invalid.
 *
 * @param TId the type of the id, can be [RawId] or [Id].
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
}
