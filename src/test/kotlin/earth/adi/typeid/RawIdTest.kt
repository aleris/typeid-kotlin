package earth.adi.typeid

import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RawIdTest {

  @Test
  fun getPrefix() {
    assertThat(RawId("prefix", UUID.randomUUID()).prefix).isEqualTo("prefix")
  }

  @Test
  fun getUuid() {
    val uuid = UUID.randomUUID()
    assertThat(RawId("prefix", uuid).uuid).isEqualTo(uuid)
  }
}
