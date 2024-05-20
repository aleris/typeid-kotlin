package earth.adi.typeid.codec

import java.util.*
import net.jqwik.api.*
import net.jqwik.api.constraints.LowerChars
import net.jqwik.api.constraints.StringLength
import org.assertj.core.api.Assertions.*

/**
 * Additional random property-based tests for the codec. Unit tests based on randomness have their
 * pitfalls, however, the specification recommends back-and-forth testing with a large number of
 * random ids.
 *
 * @see CodecSpecTest
 */
class CodecPropertyTest {
  @Property
  fun `random uuids`(@ForAll("uuids") uuid: UUID) {
    val typeId = Codec.encode("test", uuid)
    val decoded = Codec.decode(typeId)
    assertThat(decoded).isEqualTo(Decoded.Valid("test", uuid))
  }

  @Property
  fun `random valid prefixes`(@ForAll @LowerChars @StringLength(min = 1, max = 63) prefix: String) {
    val uuid = UUID.randomUUID()
    val typeId = Codec.encode(prefix, uuid)
    val decoded = Codec.decode(typeId)
    assertThat(decoded).isEqualTo(Decoded.Valid(prefix, uuid))
  }

  @Property
  fun `random ids`(
      @ForAll("uuids") uuid: UUID,
      @ForAll @LowerChars @StringLength(min = 1, max = 63) prefix: String
  ) {
    val typeId = Codec.encode(prefix, uuid)
    val decoded = Codec.decode(typeId)
    assertThat(decoded).isEqualTo(Decoded.Valid(prefix, uuid))
  }

  @Provide
  fun uuids(): Arbitrary<UUID> {
    return Arbitraries.longs().tuple2().map { longs -> UUID(longs.get1(), longs.get2()) }
  }
}
