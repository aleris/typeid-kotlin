package earth.adi.typeid.codec

import com.fasterxml.jackson.databind.util.LRUMap
import earth.adi.typeid.Id
import earth.adi.typeid.RawId
import earth.adi.typeid.TypedPrefix
import earth.adi.typeid.Validated
import java.util.*

class Factory {
  private val customPrefixes = mutableMapOf<Class<*>, TypedPrefix<*>>()
  // limit the number of prefixes to prevent prefix explosion from untrusted sources
  // in normal use cases, the cache will hold all prefixes
  private val prefixesCache = LRUMap<Class<*>, TypedPrefix<*>>(10, 1000)

  inline fun <reified TEntity> create(uuid: UUID): Id<out TEntity> {
    return create(TEntity::class.java, uuid)
  }

  fun <TEntity> create(entityType: Class<TEntity>, uuid: UUID): Id<out TEntity> {
    val typedPrefix: TypedPrefix<out TEntity> = getOrCreatePrefix(entityType)
    return Id(typedPrefix, uuid)
  }

  inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
    return parse(TEntity::class.java, id)
  }

  fun <TEntity> parse(entityType: Class<out TEntity>, id: String): Id<out TEntity> {
    val prefix = getOrCreatePrefix(entityType)
    return when (val decodedId = Codec.decode(prefix, id)) {
      is Decoded.Valid -> Id(prefix, decodedId.uuid)
      is Decoded.Invalid -> throw IllegalArgumentException(decodedId.error)
    }
  }

  fun parseToRaw(id: String): RawId {
    return when (val decodedId = Codec.decode(id)) {
      is Decoded.Valid -> RawId(decodedId.prefix, decodedId.uuid)
      is Decoded.Invalid -> throw IllegalArgumentException(decodedId.error)
    }
  }

  inline fun <reified TEntity> parseToValidated(id: String): Validated<Id<out TEntity>> {
    return parseToValidated(TEntity::class.java, id)
  }

  fun <TEntity> parseToValidated(
      entityType: Class<out TEntity>,
      id: String
  ): Validated<Id<out TEntity>> {
    val prefix = getOrCreatePrefix(entityType)
    return when (val decodedId = Codec.decode(prefix, id)) {
      is Decoded.Valid -> Validated.Valid(Id(prefix, decodedId.uuid))
      is Decoded.Invalid -> Validated.Invalid(decodedId.error)
    }
  }

  fun parseToValidatedRaw(id: String): Validated<RawId> {
    return when (val decodedId = Codec.decode(id)) {
      is Decoded.Valid -> Validated.Valid(RawId(decodedId.prefix, decodedId.uuid))
      is Decoded.Invalid -> Validated.Invalid(decodedId.error)
    }
  }

  fun <TEntity> registerCustomPrefix(
      entityType: Class<out TEntity>,
      typedPrefix: TypedPrefix<out TEntity>
  ) {
    Codec.requireValidPrefix(typedPrefix.prefix)
    customPrefixes[entityType] = typedPrefix
  }

  private fun <TEntity> getOrCreatePrefix(entityType: Class<TEntity>): TypedPrefix<out TEntity> {
    @Suppress("UNCHECKED_CAST") val prefix = prefixesCache.get(entityType) as TypedPrefix<TEntity>?
    if (prefix != null) {
      return prefix
    }

    @Suppress("UNCHECKED_CAST")
    val customPrefix = customPrefixes[entityType] as TypedPrefix<TEntity>?
    if (customPrefix != null) {
      prefixesCache.put(entityType, customPrefix)
      return customPrefix
    }

    val defaultPrefix = defaultPrefix(entityType)
    prefixesCache.put(entityType, defaultPrefix)
    return defaultPrefix
  }

  private fun <TEntity> defaultPrefix(entityType: Class<out TEntity>): TypedPrefix<out TEntity> {
    return TypedPrefix(entityType.simpleName.lowercase().removeSuffix(Id.ID_SUFFIX))
  }
}
