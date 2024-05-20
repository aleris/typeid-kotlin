package earth.adi.typeid

import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.util.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.cbor.Cbor
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import org.assertj.core.api.Assertions.assertThat
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

  @OptIn(ExperimentalSerializationApi::class)
  @Test
  fun `test kotlin serialization deserialization`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val id = RawId("user", uuid)
    val bytes = Cbor.encodeToByteArray<RawId>(id)
    val deserialized = Cbor.decodeFromByteArray<RawId>(bytes)
    assertThat(deserialized).isEqualTo(id)
  }
}
