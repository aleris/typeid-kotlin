package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.*
import com.fasterxml.jackson.databind.deser.ContextualDeserializer
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import earth.adi.typeid.Id
import earth.adi.typeid.TypeId
import java.io.IOException

/**
 * A Jackson deserializer for [Id] objects.
 *
 * @param typeId the type id instance to use for parsing the id
 * @param valueType value type of the id
 */
class IdJsonDeserializer
@JvmOverloads
constructor(private val typeId: TypeId, valueType: JavaType? = null) :
    StdDeserializer<Id<*>?>(valueType), ContextualDeserializer {
  @Throws(IOException::class, JsonProcessingException::class)
  override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Id<*> {
    val id = jp.codec.readValue(jp, String::class.java)
    return typeId.parse(valueType.bindings.getBoundType(0).rawClass, id)
  }

  override fun createContextual(
      deserializationContext: DeserializationContext?,
      beanProperty: BeanProperty?
  ): JsonDeserializer<Id<*>?> {
    val type: JavaType? =
        if (deserializationContext?.contextualType != null) deserializationContext.contextualType
        else beanProperty?.type
    return IdJsonDeserializer(typeId, type)
  }
}
