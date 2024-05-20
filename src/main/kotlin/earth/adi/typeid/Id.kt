package earth.adi.typeid

import earth.adi.typeid.codec.Codec
import earth.adi.typeid.serialization.UUIDKSerializer
import java.io.Serializable
import java.util.*

/**
 * A type-safe TypeId identifier that combines a [TypedPrefix] with a [UUID].
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
 * It is possible to create an `Id` directly, but it is recommended to use the `TypeId` factory
 * methods.
 *
 * @param TEntity the type of the associated entity with the identifier
 * @constructor Creates a new typed identifier with the specified [typedPrefix] and [uuid].
 * @property typedPrefix the typed prefix of the identifier
 * @property uuid the uuid of the identifier
 */
@kotlinx.serialization.Serializable
data class Id<TEntity>(
    val typedPrefix: TypedPrefix<out TEntity>,
    @kotlinx.serialization.Serializable(with = UUIDKSerializer::class) val uuid: UUID,
) : Serializable {
  init {
    Codec.requireValidPrefix(typedPrefix.prefix)
  }

  /**
   * Serializes the type-safe TypeId identifier to a prefixed string and encoded uuid suffix,
   * separated by _.
   */
  override fun toString(): String {
    return Codec.encode(typedPrefix.prefix, uuid)
  }
}
