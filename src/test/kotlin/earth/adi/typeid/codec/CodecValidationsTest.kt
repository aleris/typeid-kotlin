package earth.adi.typeid.codec

import earth.adi.typeid.TypeId
import earth.adi.typeid.Validated
import java.util.*
import java.util.stream.Stream
import org.assertj.core.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

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

  @ParameterizedTest
  @ArgumentsSource(InvalidTypeIdProvider::class)
  fun parseInvalid(typeIdAsString: String) {
    val result = TypeId.parseToValidatedRaw(typeIdAsString)
    assertThat(result).isInstanceOf(Validated.Invalid::class.java)
  }

  class InvalidTypeIdProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
      return Stream.of(
              "",
              "_",
              "someprefix_", // no suffix at all
              "_01h455vb4pex5vsknk084sn02q", // suffix only, but with the preceding underscore
              "__01h455vb4pex5vsknk084sn02q", // prefix is single underscore
              "_someprefix_01h455vb4pex5vsknk084sn02q", // prefix starts with underscore
              "someprefix__01h455vb4pex5vsknk084sn02q", // prefix ends with underscore
              "_someprefix__01h455vb4pex5vsknk084sn02q", // prefix starts and ends with underscore
              "sömeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'ö'
              "someprefix_01h455öb4pex5vsknk084sn02q", // suffix with 'ö'
              "someprefix_Ă01h455b4pex5vsknk084sn02q", // suffix with 'Ă' (> ascii 255) as first
              "someprefix_/01h455b4pex5vsknk084sn02q", // suffix with '/' (< ascii '0') as first
              // char
              "sOmeprefix_01h455vb4pex5vsknk084sn02q", // prefix with 'O'
              "someprefix_01h455Vb4pex5vsknk084sn02q", // suffix with 'V'
              "someprefix_01h455lb4pex5vsknk084sn02q", // suffix with 'l'
              "ssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q", // prefix with 64 chars
              "someprefix_01h455vb4pex5vsknk084sn02", // suffix with 25 chars
              "someprefix_01h455vb4pex5vsknk084sn02q2", // suffix with 27 chars
              "someprefix_81h455vb4pex5vsknk084sn02q" // leftmost suffix char is != 0-7
              )
          .map { arguments: String -> Arguments.of(arguments) }
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ValidTypeIdProvider::class)
  fun parseValid(typeIdAsString: String, expectedPrefix: String, expectedUuid: UUID) {
    val result = TypeId.parse(typeIdAsString)
    assertThat(result.prefix).isEqualTo(expectedPrefix)
    assertThat(result.uuid).isEqualTo(expectedUuid)
  }

  class ValidTypeIdProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
      return Stream.of<Arguments>(
          Arguments.of(
              "abcdefghijklmnopqrstuvw_01h455vb4pex5vsknk084sn02q",
              "abcdefghijklmnopqrstuvw",
              TEST_UUID), // prefix with allowed chars
          Arguments.of(
              "some_prefix_01h455vb4pex5vsknk084sn02q",
              "some_prefix",
              TEST_UUID), // prefix with underscore
          Arguments.of(
              "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss_01h455vb4pex5vsknk084sn02q",
              "sssssssssssssssssssssssssssssssssssssssssssssssssssssssssssssss",
              TEST_UUID) // prefix with 63 chars
          )
    }
  }

  companion object {
    private val TEST_UUID = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057")
  }
}
