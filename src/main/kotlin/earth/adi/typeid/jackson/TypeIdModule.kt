package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import earth.adi.typeid.Id
import earth.adi.typeid.RawId
import earth.adi.typeid.TypeId

/**
 * Jackson module for serializing and deserializing [Id] and [RawId] objects.
 *
 * To use the module:
 * ```
 * private val objectMapper = jacksonObjectMapper().registerModule(typeId.jacksonModule())
 * ```
 *
 * @param typeId the type id instance to use for parsing the id
 */
class TypeIdModule(typeId: TypeId) : SimpleModule() {
  init {
    addSerializer(Id::class.java, IdJsonSerializer())
    addDeserializer(Id::class.java, IdJsonDeserializer(typeId))

    addSerializer(RawId::class.java, RawIdJsonSerializer())
    addDeserializer(RawId::class.java, RawIdJsonDeserializer())
  }

  override fun version(): Version {
    return Version(1, 0, 0, null, null, null)
  }

  override fun getModuleName(): String {
    return MODULE_NAME
  }

  companion object {
    private const val MODULE_NAME = "TypeIdModule"
  }
}
