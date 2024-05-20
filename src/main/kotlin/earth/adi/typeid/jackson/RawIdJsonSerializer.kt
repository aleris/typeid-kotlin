package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import earth.adi.typeid.RawId
import java.io.IOException

/**
 * A Jackson serializer for [RawId] objects.
 *
 * @param t type of the id
 */
class RawIdJsonSerializer @JvmOverloads constructor(t: Class<RawId?>? = null) :
    StdSerializer<RawId?>(t) {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun serialize(value: RawId?, jgen: JsonGenerator, provider: SerializerProvider?) {
    if (value == null) {
      jgen.writeNull()
      return
    }
    jgen.writeString(value.toString())
  }
}
