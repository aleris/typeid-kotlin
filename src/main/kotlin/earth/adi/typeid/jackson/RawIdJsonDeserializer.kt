package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import earth.adi.typeid.RawId
import earth.adi.typeid.TypeId
import java.io.IOException

/** A Jackson deserializer for [RawId] objects. */
class RawIdJsonDeserializer : StdDeserializer<RawId?>(RawId::class.java) {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): RawId {
    val id = jp.codec.readValue(jp, String::class.java)
    return TypeId.parse(id)
  }
}
