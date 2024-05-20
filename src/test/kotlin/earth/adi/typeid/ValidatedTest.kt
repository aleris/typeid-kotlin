package earth.adi.typeid

import earth.adi.typeid.testentities.User
import earth.adi.typeid.testentities.UserId
import java.util.concurrent.atomic.AtomicBoolean
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ValidatedTest {
  private val id = TypeId.randomId<User>()
  private val valid = Validated.Valid(id)
  private val errorMessage = "err"
  private val invalid = Validated.Invalid<UserId>(errorMessage)
  private val otherId = TypeId.randomId<User>()

  @Test
  fun `test valid`() {
    assertThat(valid.id).isEqualTo(id)
  }

  @Test
  fun `test invalid`() {
    assertThat(invalid.error).isEqualTo(errorMessage)
  }

  @Test
  fun `orElse supplier valid`() {
    assertThat(valid.orElse { otherId }).isEqualTo(id)
  }

  @Test
  fun `orElse supplier invalid`() {
    assertThat(invalid.orElse { otherId }).isEqualTo(otherId)
  }

  @Test
  fun `orElse direct value valid`() {
    assertThat(valid.orElse(otherId)).isEqualTo(id)
  }

  @Test
  fun `orElse direct value invalid`() {
    assertThat(invalid.orElse(otherId)).isEqualTo(otherId)
  }

  @Test
  fun `map valid`() {
    val mapped = valid.map { it.uuid }
    assertThat(mapped).isEqualTo(Validated.Valid(id.uuid))
  }

  @Test
  fun `map invalid`() {
    val mapped = invalid.map { it.uuid }
    assertThat(mapped).isEqualTo(invalid)
  }

  @Test
  fun `filter true supplier valid`() {
    val filtered = valid.filter({ "x" }) { true }
    assertThat(filtered).isEqualTo(valid)
  }

  @Test
  fun `filter false supplier valid`() {
    val filtered = valid.filter({ "x" }) { false }
    assertThat(filtered).isEqualTo(Validated.Invalid<UserId>("x"))
  }

  @Test
  fun `filter supplier invalid`() {
    val filtered = invalid.filter({ "x" }) { true }
    assertThat(filtered).isEqualTo(invalid)
  }

  @Test
  fun `filter direct message`() {
    val filtered = valid.filter("x") { false }
    assertThat(filtered).isEqualTo(Validated.Invalid<UserId>("x"))
  }

  @Test
  fun `ifValid valid`() {
    val called = AtomicBoolean(false)
    valid.ifValid { called.set(true) }
    assertThat(called.get()).isTrue()
  }

  @Test
  fun `ifValid invalid`() {
    val called = AtomicBoolean(false)
    invalid.ifValid { called.set(true) }
    assertThat(called.get()).isFalse()
  }

  @Test
  fun `ifInvalid valid`() {
    val called = AtomicBoolean(false)
    valid.ifInvalid { called.set(true) }
    assertThat(called.get()).isFalse()
  }

  @Test
  fun `ifInvalid invalid`() {
    val called = AtomicBoolean(false)
    invalid.ifInvalid { called.set(true) }
    assertThat(called.get()).isTrue()
  }
}
