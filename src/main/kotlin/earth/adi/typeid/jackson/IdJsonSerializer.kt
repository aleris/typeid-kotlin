package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import earth.adi.typeid.Id
import java.io.IOException

/**
 * A Jackson serializer for [Id] objects.
 *
 * @param t type of the id
 */
class IdJsonSerializer @JvmOverloads constructor(t: Class<Id<*>?>? = null) :
    StdSerializer<Id<*>?>(t) {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun serialize(value: Id<*>?, jgen: JsonGenerator, provider: SerializerProvider?) {
    if (value == null) {
      jgen.writeNull()
      return
    }
    jgen.writeString(value.toString())
  }
}
