package earth.adi.typeid

import earth.adi.typeid.codec.Codec
import earth.adi.typeid.codec.Decoded
import java.util.*
import net.jqwik.api.*
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * Additional prefix and suffix validation tests for the codec, not covered by spec tests in
 * [CodecSpecTest].
 */
class CodecValidationsTest {
  @Test
  fun `prefix must not contain invalid ascii chars`() {
    assertThat(Codec.decode("/_a")).isInstanceOf(Decoded.Invalid::class.java)
  }

  @Test
  fun `suffix must not contain invalid ascii chars`() {
    assertThat(Codec.decode("abc_/")).isInstanceOf(Decoded.Invalid::class.java)
  }

  @Test
  fun `suffix must start with a valid char`() {
    assertThat(Codec.decode("abc_Ăaaaaaaaaaaaaaaaaaaaaaaaaa"))
        .isInstanceOf(Decoded.Invalid::class.java)
  }

  @Test
  fun `suffix must not contain non-ascii chars`() {
    assertThat(Codec.decode("abc_1Ăaaaaaaaaaaaaaaaaaaaaaaaa"))
        .isInstanceOf(Decoded.Invalid::class.java)
  }

  @Test
  fun `requireValidPrefix valid`() {
    assertThatCode { Codec.requireValidPrefix("abc") }.doesNotThrowAnyException()
  }

  @Test
  fun `requireValidPrefix invalid`() {
    assertThatThrownBy { Codec.requireValidPrefix("/") }
        .isInstanceOf(IllegalArgumentException::class.java)
  }
}
