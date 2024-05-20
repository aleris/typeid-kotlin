package earth.adi.typeid

import earth.adi.typeid.codec.Codec
import earth.adi.typeid.serialization.UUIDKSerializer
import java.io.Serializable
import java.util.UUID

/**
 * Represents a type-unsafe TypeId identifier that contains a string prefix and an uuid.
 *
 * @constructor Creates a new RawId with the specified [prefix] and [uuid].
 * @property prefix the prefix of the identifier
 * @property uuid the uuid of the identifier
 */
@kotlinx.serialization.Serializable
data class RawId(
    val prefix: String,
    @kotlinx.serialization.Serializable(with = UUIDKSerializer::class) val uuid: UUID,
) : Serializable {
  init {
    Codec.requireValidPrefix(prefix)
  }

  /**
   * Serializes the TypeId identifier to a prefixed string and encoded uuid suffix, separated by _.
   */
  override fun toString(): String {
    return Codec.encode(prefix, uuid)
  }
}
