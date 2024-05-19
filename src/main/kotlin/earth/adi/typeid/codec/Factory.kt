package earth.adi.typeid.codec

import com.fasterxml.jackson.databind.util.LRUMap
import earth.adi.typeid.Id
import earth.adi.typeid.RawId
import earth.adi.typeid.TypedPrefix
import earth.adi.typeid.Validated
import java.util.*

/**
 * Factory for creating and parsing [Id]s and [RawId]s. This class is not meant to be used directly,
 * but through the [earth.adi.typeid.TypeId].
 *
 * Has an internal cache for prefixes to avoid creating the same prefix multiple times, so it is
 * better to use a single instance of this class.
 */
class Factory {
  private val customPrefixes = mutableMapOf<Class<*>, TypedPrefix<*>>()
  // limit the number of prefixes to prevent prefix explosion from untrusted sources
  // in normal use cases, the cache will hold all prefixes
  private val prefixesCache = LRUMap<Class<*>, TypedPrefix<*>>(10, 1000)

  /**
   * Creates a new [Id] for the given entity type and UUID.
   *
   * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
   * is derived from the lowercase simple class name of the entity type. For example, the prefix for
   * `User` is `user`.
   *
   * If you want to customize the prefix, use [registerCustomPrefix].
   *
   * @param TEntity the entity type
   * @param uuid the UUID
   * @return the new [Id]
   */
  inline fun <reified TEntity> create(uuid: UUID): Id<out TEntity> {
    return create(TEntity::class.java, uuid)
  }

  /**
   * Creates a new [Id] for the given entity type and UUID.
   *
   * By default, the prefix is derived from the lowercase simple class name of the entity type. For
   * example, the prefix for `User` is `user`.
   *
   * If you want to customize the prefix, use [registerCustomPrefix].
   *
   * @param TEntity the entity type
   * @param entityType the entity type class
   * @param uuid the UUID
   * @return the new [Id]
   */
  fun <TEntity> create(entityType: Class<TEntity>, uuid: UUID): Id<out TEntity> {
    val typedPrefix: TypedPrefix<out TEntity> = getOrCreatePrefix(entityType)
    return Id(typedPrefix, uuid)
  }

  /**
   * Parses a typed [Id] from a string.
   *
   * @param TEntity the entity type
   * @param id the string typeid representation of the [Id]
   * @return the parsed [Id]
   * @throws IllegalArgumentException if the string is not a valid [Id]
   */
  inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
    return parse(TEntity::class.java, id)
  }

  /**
   * Parses a typed [Id] from a string.
   *
   * @param TEntity the entity type
   * @param entityType the entity type class
   * @param id the string typeid representation of the [Id]
   * @return the parsed [Id]
   * @throws IllegalArgumentException if the string is not a valid [Id]
   */
  fun <TEntity> parse(entityType: Class<out TEntity>, id: String): Id<out TEntity> {
    val prefix = getOrCreatePrefix(entityType)
    return when (val decodedId = Codec.decode(prefix, id)) {
      is Decoded.Valid -> Id(prefix, decodedId.uuid)
      is Decoded.Invalid -> throw IllegalArgumentException(decodedId.error)
    }
  }

  /**
   * Parses an untyped [RawId] from a string.
   *
   * @param id the string typeid representation of the [RawId]
   * @return the parsed [RawId]
   * @throws IllegalArgumentException if the string is not a valid [RawId]
   */
  fun parseToRaw(id: String): RawId {
    return when (val decodedId = Codec.decode(id)) {
      is Decoded.Valid -> RawId(decodedId.prefix, decodedId.uuid)
      is Decoded.Invalid -> throw IllegalArgumentException(decodedId.error)
    }
  }

  /**
   * Parses a typed [Id] from a string and returns a [Validated] result. If the string is valid a
   * [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
   *
   * @param TEntity the entity type
   * @param id the string typeid representation of the [Id]
   * @return the result of the parsing as a [Validated] result
   */
  inline fun <reified TEntity> parseToValidated(id: String): Validated<Id<out TEntity>> {
    return parseToValidated(TEntity::class.java, id)
  }

  /**
   * Parses a typed [Id] from a string and returns a [Validated] result. If the string is valid a
   * [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
   *
   * @param TEntity the entity type
   * @param entityType the entity type class
   * @param id the string typeid representation of the [Id]
   * @return the result of the parsing as a [Validated] result
   */
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

  /**
   * Parses an untyped [RawId] from a string and returns a [Validated] result. If the string is
   * valid a [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
   *
   * @param id the string typeid representation of the [RawId]
   * @return the result of the parsing as a [Validated] result
   */
  fun parseToValidatedRaw(id: String): Validated<RawId> {
    return when (val decodedId = Codec.decode(id)) {
      is Decoded.Valid -> Validated.Valid(RawId(decodedId.prefix, decodedId.uuid))
      is Decoded.Invalid -> Validated.Invalid(decodedId.error)
    }
  }

  /**
   * Registers a custom prefix for the given entity type. Can be used to customize the prefix for a
   * specific entity type.
   *
   * By default, the prefix is derived from the lowercase simple class name of the id. For example,
   * the prefix for `UserId` is `user`.
   *
   * @param TEntity the entity type
   * @param typedPrefix the custom prefix to use for the entity type
   */
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
    return TypedPrefix(entityType.simpleName.lowercase())
  }
}
