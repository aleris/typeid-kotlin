package earth.adi.typeid.jackson

import com.fasterxml.jackson.core.Version
import com.fasterxml.jackson.databind.module.SimpleModule
import earth.adi.typeid.Id
import earth.adi.typeid.TypeId

class TypeIdModule(typeId: TypeId) : SimpleModule() {
  init {
    addSerializer(Id::class.java, IdJsonSerializer())
    addDeserializer(Id::class.java, IdJsonDeserializer(typeId))
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
