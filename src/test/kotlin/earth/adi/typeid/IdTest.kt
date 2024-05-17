package earth.adi.typeid

import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class IdTest {
  @Test
  fun `can create raw id`() {
    val id = Id<String>(TypedPrefix("user"), UUID.randomUUID())
    assertThat(id.toString()).startsWith("user_")
  }

  @Test
  fun `prefix must be valid`() {
    assertThatThrownBy { Id<String>(TypedPrefix(""), UUID.randomUUID()) }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `get prefix`() {
    val id = Id<String>(TypedPrefix("user"), UUID.randomUUID())
    assertThat(id.typedPrefix.prefix).isEqualTo("user")
  }

  @Test
  fun `get uuid`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val id = Id<String>(TypedPrefix("user"), uuid)
    assertThat(id.uuid).isEqualTo(uuid)
  }
}
