package earth.adi.typeid

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

internal class JavaTypeTest {
  @Test
  fun of() {
    val javaType = JavaType.of(String::class.java)
    assertThat(javaType.clazz).isEqualTo(String::class.java)
  }

  @Test
  fun get() {
    val javaType = JavaType.of(String::class.java)
    assertThat(javaType.clazz).isEqualTo(String::class.java)
  }
}
