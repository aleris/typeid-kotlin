package earth.adi.typeid

import earth.adi.typeid.codec.Codec
import java.io.Serializable
import java.util.*

/**
 * A typed identifier that combines a [TypedPrefix] with a [UUID].
 *
 * Usage:
 * ```
 * // Define your identifiable entity type:
 * data class User(val id: UserId)
 *
 * // Define a typealias for the user id.
 * typealias UserId = Id<out User>
 *
 * // Then, to create a random user id:
 * val userId = TypeId.randomId<User>()
 * println(userId) // prints something like user_01hy0cmx53fe8tr2dy37d42avj
 *
 * // To try parse a user id from a string
 * val userId = TypeId.parse<User>("user_01hy0cmx53fe8tr2dy37d42avj") // throws an exception if the id is invalid
 *
 * // Or to parse a user id from a string, returns a validated result:
 * val userId = TypeId.parseToValidated<User>("user_01hy0cmx53fe8tr2dy37d42avj")
 * if (userId is Validated.Valid) {
 *   // ...
 * }
 * ```
 *
 * @param TEntity the type of the associated entity with the identifier
 * @constructor Creates a new typed identifier with the specified [typedPrefix] and [uuid].
 * @property typedPrefix the typed prefix of the identifier
 * @property uuid the uuid of the identifier
 */
data class Id<TEntity>(val typedPrefix: TypedPrefix<out TEntity>, val uuid: UUID) : Serializable {
  init {
    Codec.requireValidPrefix(typedPrefix.prefix)
  }

  /**
   * Serializes the typed identifier to a prefixed string and encoded uuid suffix, separated by _.
   */
  override fun toString(): String {
    return Codec.encode(typedPrefix.prefix, uuid)
  }

  companion object {
    /**
     * The suffix for all id types, used for creating default prefixes from the identifier names.
     */
    const val ID_SUFFIX = "Id"
  }
}
