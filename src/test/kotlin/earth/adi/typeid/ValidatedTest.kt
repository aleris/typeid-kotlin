package earth.adi.typeid

import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidatedTest {
  @Test
  fun `test valid`() {
    val id = Id<String>(TypedPrefix("user"), UUID.randomUUID())
    val validated = Validated.Valid(id)
    assertThat(validated.id).isEqualTo(id)
  }

  @Test
  fun `test invalid`() {
    val error = "err"
    val validated = Validated.Invalid<String>(error)
    assertThat(validated.error).isEqualTo(error)
  }
}
