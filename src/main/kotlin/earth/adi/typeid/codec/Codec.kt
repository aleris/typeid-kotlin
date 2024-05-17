package earth.adi.typeid.codec

import earth.adi.typeid.TypedPrefix
import java.util.*

// original source from
// https://github.com/fxlae/typeid-java/blob/main/lib/src/main/java/de/fxlae/typeid/lib/TypeIdLib.java
object Codec {
  private const val SEPARATOR = '_'
  private const val SUFFIX_ALPHABET = "0123456789abcdefghjkmnpqrstvwxyz"
  private const val PREFIX_MAX_LENGTH = 63
  private const val SUFFIX_LENGTH = 26

  // inspired by base32.go from the official go implementation
  // https://github.com/jetpack-io/typeid-go/blob/main/base32/base32.go
  // lookup: [ascii pos] -> binary representation of block
  // sentinel value for characters that are not part of the alphabets
  private const val NOOP = Long.MAX_VALUE

  // these values currently are longs because they are directly shifted into the two longs
  // of a UUID
  // spotless:off
  private val SUFFIX_LOOKUP = longArrayOf(
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, 0x00, 0x01,  // 0, 1
    0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, NOOP, NOOP,  // 2, 3, 4, 5, 6, 7, 8, 9
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, 0x0A, 0x0B, 0x0C,  // a, b, c
    0x0D, 0x0E, 0x0F, 0x10, 0x11, NOOP, 0x12, 0x13, NOOP, 0x14,  // d, e, f, g, h, j, k, m
    0x15, NOOP, 0x16, 0x17, 0x18, 0x19, 0x1A, NOOP, 0x1B, 0x1C,  // n, p, q, r, s, value, v, w
    0x1D, 0x1E, 0x1F, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,  // x, y, z
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP, NOOP,
    NOOP, NOOP, NOOP, NOOP, NOOP, NOOP
  )
  // spotless:on

  fun encode(prefix: String, uuid: UUID): String {
    val msb = uuid.mostSignificantBits
    val lsb = uuid.leastSignificantBits

    val sb: StringBuilder
    if (prefix.isEmpty()) {
      sb = StringBuilder(26)
    } else {
      sb = StringBuilder(27 + prefix.length)
      sb.append(prefix).append(SEPARATOR)
    }

    // encode the MSBs except the last bit, as the block it belongs to overlaps with the LSBs
    sb.append(SUFFIX_ALPHABET[(msb ushr 61).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 56).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 51).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 46).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 41).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 36).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 31).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 26).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 21).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 16).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 11).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 6).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(msb ushr 1).toInt() and 0x1F])

    // encode the overlap between MSBs (1 bit) and LSBs (4 bits)
    val overlap = ((msb and 0x1L) shl 4) or (lsb ushr 60)
    sb.append(SUFFIX_ALPHABET[overlap.toInt()])

    // encode the rest of LSBs
    sb.append(SUFFIX_ALPHABET[(lsb ushr 55).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 50).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 45).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 40).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 35).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 30).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 25).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 20).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 15).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 10).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[(lsb ushr 5).toInt() and 0x1F])
        .append(SUFFIX_ALPHABET[lsb.toInt() and 0x1F])

    return sb.toString()
  }

  fun requireValidPrefix(prefix: String) {
    require(prefix.isNotBlank()) { "Prefix must not be blank" }
    val validatedPrefix = validatePrefixOnInput(prefix, prefix.length)
    require(validatedPrefix.isValid) { validatedPrefix }
  }

  fun decode(expectedPrefix: TypedPrefix<*>, text: String): Decoded {
    return when (val decoded = decode(text)) {
      is Decoded.Valid -> {
        if (decoded.prefix != expectedPrefix.prefix) {
          Decoded.Invalid(
              "Prefix mismatch, expected '${expectedPrefix.prefix}' but got '${decoded.prefix}'")
        } else {
          decoded
        }
      }
      is Decoded.Invalid -> decoded
    }
  }

  fun decode(text: String): Decoded {
    val separatorIndex = text.lastIndexOf(SEPARATOR)

    // empty prefix, but with unexpected first char as separator
    if (separatorIndex == 0) {
      return Decoded.Invalid("Id with empty prefix must not contain the separator '$SEPARATOR'")
    }

    validatePrefixOnInput(text, separatorIndex)
        .takeIf { !it.isValid }
        ?.let {
          return Decoded.Invalid(it.error)
        }

    validateSuffixOnInput(text, separatorIndex)
        .takeIf { !it.isValid }
        ?.let {
          return Decoded.Invalid(it.error)
        }

    return Decoded.Valid(
        extractPrefix(text, separatorIndex),
        decodeSuffixOnInput(text, separatorIndex),
    )
  }

  private fun decodeSuffixOnInput(input: String, separatorIndex: Int): UUID {
    val start = if ((separatorIndex == -1)) 0 else separatorIndex + 1

    var lsb: Long = 0
    var msb: Long = 0

    // decode characters [25] to [14] into the LSBs
    lsb = lsb or (SUFFIX_LOOKUP[input[25 + start].code])
    lsb = lsb or ((SUFFIX_LOOKUP[input[24 + start].code]) shl 5)
    lsb = lsb or ((SUFFIX_LOOKUP[input[23 + start].code]) shl 10)
    lsb = lsb or ((SUFFIX_LOOKUP[input[22 + start].code]) shl 15)
    lsb = lsb or ((SUFFIX_LOOKUP[input[21 + start].code]) shl 20)
    lsb = lsb or ((SUFFIX_LOOKUP[input[20 + start].code]) shl 25)
    lsb = lsb or ((SUFFIX_LOOKUP[input[19 + start].code]) shl 30)
    lsb = lsb or ((SUFFIX_LOOKUP[input[18 + start].code]) shl 35)
    lsb = lsb or ((SUFFIX_LOOKUP[input[17 + start].code]) shl 40)
    lsb = lsb or ((SUFFIX_LOOKUP[input[16 + start].code]) shl 45)
    lsb = lsb or ((SUFFIX_LOOKUP[input[15 + start].code]) shl 50)
    lsb = lsb or ((SUFFIX_LOOKUP[input[14 + start].code]) shl 55)

    // decode the overlap between LSBs and MSBs (character [13])
    val bitsAtOverlap = SUFFIX_LOOKUP[input[13 + start].code]
    lsb = lsb or ((bitsAtOverlap and 0xFL) shl 60)
    msb = msb or ((bitsAtOverlap and 0x10L) ushr 4)

    // decode characters [12] to [0] into the MSBs
    msb = msb or ((SUFFIX_LOOKUP[input[12 + start].code]) shl 1)
    msb = msb or ((SUFFIX_LOOKUP[input[11 + start].code]) shl 6)
    msb = msb or ((SUFFIX_LOOKUP[input[10 + start].code]) shl 11)
    msb = msb or ((SUFFIX_LOOKUP[input[9 + start].code]) shl 16)
    msb = msb or ((SUFFIX_LOOKUP[input[8 + start].code]) shl 21)
    msb = msb or ((SUFFIX_LOOKUP[input[7 + start].code]) shl 26)
    msb = msb or ((SUFFIX_LOOKUP[input[6 + start].code]) shl 31)
    msb = msb or ((SUFFIX_LOOKUP[input[5 + start].code]) shl 36)
    msb = msb or ((SUFFIX_LOOKUP[input[4 + start].code]) shl 41)
    msb = msb or ((SUFFIX_LOOKUP[input[3 + start].code]) shl 46)
    msb = msb or ((SUFFIX_LOOKUP[input[2 + start].code]) shl 51)
    msb = msb or ((SUFFIX_LOOKUP[input[1 + start].code]) shl 56)
    msb = msb or ((SUFFIX_LOOKUP[input[start].code]) shl 61)

    return UUID(msb, lsb)
  }

  private fun extractPrefix(input: String, separatorIndex: Int): String {
    return if (separatorIndex == -1) {
      ""
    } else {
      input.substring(0, separatorIndex)
    }
  }

  private data class ValidatedSuffix(
      val isValid: Boolean,
      val error: String = "",
  )

  // validates the suffix without creating an intermediary object for it
  private fun validateSuffixOnInput(input: String, separatorIndex: Int): ValidatedSuffix {
    val start = if ((separatorIndex != -1)) separatorIndex + 1 else 0

    val suffixLength = input.length - start
    if (suffixLength != SUFFIX_LENGTH) {
      return ValidatedSuffix(false, "Suffix with illegal length, must be $SUFFIX_LENGTH")
    }

    val startCode = input[start].code
    if (startCode >= SUFFIX_LOOKUP.size || ((SUFFIX_LOOKUP[startCode] ushr 3) and 0x3L) > 0) {
      return ValidatedSuffix(false, "Illegal leftmost suffix character, must be one of [01234567]")
    }

    for (i in start until input.length) {
      val c = input[i]
      val code = c.code
      if (code >= SUFFIX_LOOKUP.size || SUFFIX_LOOKUP[code] == NOOP) {
        return ValidatedSuffix(
            false, "Illegal character in suffix, must be one of [$SUFFIX_ALPHABET]")
      }
    }

    return ValidatedSuffix(true)
  }

  // validates the prefix without creating an intermediary object for it
  private fun validatePrefixOnInput(input: String, separatorIndex: Int): ValidatedSuffix {
    // empty prefix, no separator

    if (separatorIndex == -1) {
      return ValidatedSuffix(true)
    }

    if (separatorIndex > PREFIX_MAX_LENGTH) {
      return ValidatedSuffix(
          false,
          "The prefix can't be $separatorIndex characters, it needs to be $PREFIX_MAX_LENGTH characters or less")
    }

    if (input[0] == SEPARATOR || input[separatorIndex - 1] == SEPARATOR) {
      return ValidatedSuffix(false, "Prefix must not start or end with '$SEPARATOR'")
    }

    for (i in 0 until separatorIndex) {
      val c = input[i]
      if (c !in 'a'..'z' && c != SEPARATOR) {
        return ValidatedSuffix(false, "Illegal character in prefix, must be one of [a-z$SEPARATOR]")
      }
    }

    return ValidatedSuffix(true)
  }
}
