package earth.adi.typeid

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import earth.adi.typeid.codec.Codec
import earth.adi.typeid.codec.Decoded
import java.util.*
import java.util.stream.Stream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.ArgumentsProvider
import org.junit.jupiter.params.provider.ArgumentsSource

/**
 * This class tests against the specification published on
 * [github.com/jetpack-io/typeid](https://github.com/jetpack-io/typeid/tree/main/spec), specifically
 * against [valid.yml](https://github.com/jetpack-io/typeid/blob/main/spec/valid.yml) and
 * [invalid.yml](https://github.com/jetpack-io/typeid/blob/main/spec/invalid.yml).
 */
class CodecSpecTest {
  @ParameterizedTest
  @ArgumentsSource(SpecValidProvider::class)
  fun testEncodeAgainstSpecValid(
      name: String,
      expectedTypeIdAsString: String,
      prefix: String,
      uuid: UUID
  ) {
    val typeId = Codec.encode(prefix, uuid)
    assertThat(typeId).describedAs(name).isEqualTo(expectedTypeIdAsString)
  }

  @ParameterizedTest
  @ArgumentsSource(SpecValidProvider::class)
  fun testDecodeAgainstSpecValid(
      name: String,
      typeIdAsString: String,
      expectedPrefix: String,
      expectedUuid: UUID
  ) {
    val typeId = Codec.decode(typeIdAsString)
    assertThat(typeId).describedAs(name).isEqualTo(Decoded.Valid(expectedPrefix, expectedUuid))
  }

  @ParameterizedTest
  @ArgumentsSource(SpecInvalidProvider::class)
  fun testDecodeAgainstSpecInvalid(name: String, typeIdAsString: String, description: String) {
    val decoded = Codec.decode(typeIdAsString)
    assertThat(decoded)
        .describedAs(name)
        .withFailMessage(description)
        .isInstanceOf(Decoded.Invalid::class.java)
  }

  class SpecValidProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
      return loadSpec("/spec/valid.yml", SpecValid::class.java).stream().map { s: SpecValid ->
        Arguments.of(s.name, s.typeid, s.prefix, UUID.fromString(s.uuid))
      }
    }
  }

  class SpecInvalidProvider : ArgumentsProvider {
    override fun provideArguments(context: ExtensionContext): Stream<out Arguments> {
      return loadSpec("/spec/invalid.yml", SpecInvalid::class.java).stream().map { s: SpecInvalid ->
        Arguments.of(s.name, s.typeid, s.description)
      }
    }
  }

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  internal class SpecValid {
    var name: String? = null
    var typeid: String? = null
    var prefix: String? = null
    var uuid: String? = null
  }

  @JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
  internal class SpecInvalid {
    var name: String? = null
    var typeid: String? = null
    var description: String? = null
  }

  companion object {
    fun <T> loadSpec(path: String?, clazz: Class<T>?): List<T> {
      val mapper = ObjectMapper(YAMLFactory())
      val javaType = mapper.typeFactory.constructCollectionType(MutableList::class.java, clazz)
      return mapper.readValue(CodecSpecTest::class.java.getResourceAsStream(path), javaType)
    }
  }
}
