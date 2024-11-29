package earth.adi.typeid.jackson

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.Test

class IdJsonSerializerTest {
  @Test
  fun `constructor with null does not throw`() {
    assertThatCode { IdJsonSerializer(null) }.doesNotThrowAnyException()
  }

  @Test
  fun `constructor with default does not throw`() {
    assertThatCode { IdJsonSerializer() }.doesNotThrowAnyException()
  }
}
