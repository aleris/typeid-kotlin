package earth.adi.typeid.codec

import java.util.*

/** Represents a valid or invalid decoded type id. */
sealed class Decoded {
  /** A valid decoded type id that contains the decoded [prefix] and [uuid]. */
  data class Valid(val prefix: String, val uuid: UUID) : Decoded()

  /** An invalid decoded type id with an [error] message. */
  data class Invalid(val error: String) : Decoded()
}
