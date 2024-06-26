package earth.adi.typeid

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.UUID
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
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

  @Test
  fun `test java serialization deserialization`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val id = Id<String>(TypedPrefix("user"), uuid)
    ByteArrayOutputStream().use { outputStream ->
      ObjectOutputStream(outputStream).writeObject(id)
      ObjectInputStream(outputStream.toByteArray().inputStream()).use {
        @Suppress("UNCHECKED_CAST") val deserializedId = it.readObject() as Id<String>
        assertThat(deserializedId).isEqualTo(id)
      }
    }
  }

  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun `test kotlin serialization deserialization`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val id = Id<String>(TypedPrefix("user"), uuid)
    val bytes = Cbor.encodeToByteArray<Id<String>>(id)
    val deserialized = Cbor.decodeFromByteArray<Id<String>>(bytes)
    assertThat(deserialized).isEqualTo(id)
  }
}
