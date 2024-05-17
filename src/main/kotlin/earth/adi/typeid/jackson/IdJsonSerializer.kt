package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import earth.adi.typeid.Id
import java.io.IOException

class IdJsonSerializer @JvmOverloads constructor(t: Class<Id<*>?>? = null) :
    StdSerializer<Id<*>?>(t) {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun serialize(value: Id<*>?, jgen: JsonGenerator, provider: SerializerProvider?) {
    jgen.writeString(value.toString())
  }
}
