package earth.adi.typeid

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
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

  @Test
  fun `test serialization deserialization`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val id = RawId("user", uuid)
    ByteArrayOutputStream().use { outputStream ->
      ObjectOutputStream(outputStream).writeObject(id)
      ObjectInputStream(outputStream.toByteArray().inputStream()).use {
        val deserializedId = it.readObject() as RawId
        assertThat(deserializedId).isEqualTo(id)
      }
    }
  }
}
