package earth.adi.typeid

import com.fasterxml.uuid.Generators
import earth.adi.typeid.codec.Factory
import earth.adi.typeid.jackson.TypeIdModule
import java.util.*
import java.util.function.Supplier

/**
 * Facade for creating and parsing type-safe TypeId identifiers.
 *
 * Is recommended to use the factory function [typeId] to create a new instance of [TypeId] and
 * reuse it throughout the application because it has an internal cache of prefixes.
 *
 * Usage:
 * ```
 * val typeId = typeId()
 *   // Optionally customize the UUID generator
 *   .withUUIDGenerator { Generators.randomBasedGenerator().generate() }
 *   // Optionally register a custom prefix for a specific entity type
 *   .withCustomPrefix(TypedPrefix<Organization>("org"))
 *
 * val userId = typeId.randomId<User>()
 * println(userId) // prints something like user_01hy0cmx53fe8tr2dy37d42avj
 *
 * val organizationId = typeId.randomId<Organization>()
 * println(organizationId) // prints something like org_01hy0cmx53fe8tr2dy37d42avj
 *
 * val parsedUserId = typeId.parse<User>("user_01hy0cmx53fe8tr2dy37d42avj") // throws an exception if the id is invalid
 * ```
 *
 * Alternatively you can also use the default [TypeId] methods directly. Internally, this uses a
 * static singleton [TypeId] instance.
 *
 * Usage:
 * ```
 * val userId = TypeId.randomId<User>()
 * ```
 */
class TypeId(private var uuidGenerator: Supplier<UUID>) {
  /** The internal factory for creating and parsing identifiers. */
  val factory = Factory() // need to be public for inline reified functions

  /**
   * Creates a new random [Id] for the given entity type.
   *
   * Uses the default UUIDv7 generator for generating a new UUID or the one set with
   * [withUUIDGenerator].
   *
   * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
   * is derived from the lowercase simple class name of the entity type. For example, the prefix for
   * `User` is `user`.
   *
   * If you want to customize the prefix, use [withCustomPrefix].
   *
   * @param TEntity the entity type
   * @return the new [Id]
   */
  inline fun <reified TEntity> randomId(): Id<out TEntity> {
    return randomId(TEntity::class.java)
  }

  /**
   * Creates a new random [Id] for the given entity type.
   *
   * Uses the default UUIDv7 generator for generating a new UUID or the one set with
   * [withUUIDGenerator].
   *
   * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
   * is derived from the lowercase simple class name of the entity type. For example, the prefix for
   * `User` is `user`.
   *
   * If you want to customize the prefix, use [withCustomPrefix].
   *
   * @param entityType the inferred entity type class
   * @return the new [Id]
   */
  fun <TEntity> randomId(entityType: Class<out TEntity>): Id<out TEntity> {
    return factory.create(entityType, uuidGenerator.get())
  }

  /**
   * Creates a new random untyped [RawId] with the given prefix.
   *
   * Uses the default UUIDv7 generator for generating a new UUID or the one set with
   * [withUUIDGenerator].
   *
   * @param prefix the prefix of the identifier
   * @return the new [RawId]
   */
  fun randomId(prefix: String): RawId {
    return RawId(prefix, uuidGenerator.get())
  }

  /**
   * Creates a new [Id] for the given entity type and UUID.
   *
   * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
   * is derived from the lowercase simple class name of the entity type. For example, the prefix for
   * `User` is `user`.
   *
   * If you want to customize the prefix, use [withCustomPrefix].
   *
   * @param TEntity the inferred entity type
   * @param uuid the UUID
   * @return the new [Id]
   */
  inline fun <reified TEntity> of(uuid: UUID): Id<out TEntity> {
    return factory.create(uuid)
  }

  /**
   * Creates a new [Id] for the given entity type and UUID.
   *
   * By default, the prefix is derived from the lowercase simple class name of the entity type. For
   * example, the prefix for `User` is `user`.
   *
   * If you want to customize the prefix, use [withCustomPrefix].
   *
   * @param TEntity the entity type
   * @param entityType the entity type class
   * @param uuid the UUID
   * @return the new [Id]
   */
  fun <TEntity> of(entityType: Class<out TEntity>, uuid: UUID): Id<out TEntity> {
    return factory.create(entityType, uuid)
  }

  /**
   * Creates a new untyped [RawId] with the given prefix and UUID.
   *
   * @param prefix the prefix of the identifier
   * @param uuid the UUID
   * @return the new [RawId]
   */
  fun of(prefix: String, uuid: UUID): RawId {
    return RawId(prefix, uuid)
  }

  /**
   * Parses a typed [Id] from a string.
   *
   * @param TEntity the inferred entity type
   * @param id the string typeid representation of the [Id]
   * @return the parsed [Id]
   * @throws IllegalArgumentException if the string is not a valid [Id]
   */
  inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
    return factory.parse<TEntity>(id)
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
    return factory.parse(entityType, id)
  }

  /**
   * Parses an untyped [RawId] from a string.
   *
   * @param id the string typeid representation of the [RawId]
   * @return the parsed [RawId]
   * @throws IllegalArgumentException if the string is not a valid [RawId]
   */
  fun parse(id: String): RawId {
    return factory.parseToRaw(id)
  }

  /**
   * Parses a typed [Id] from a string and returns a [Validated] result. If the string is valid a
   * [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
   *
   * @param TEntity the inferred entity type
   * @param id the string typeid representation of the [Id]
   * @return the result of the parsing as a [Validated] result
   */
  inline fun <reified TEntity> parseToValidated(id: String): Validated<Id<out TEntity>> {
    return factory.parseToValidated<TEntity>(id)
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
    return factory.parseToValidated(entityType, id)
  }

  /**
   * Parses an untyped [RawId] from a string and returns a [Validated] result. If the string is
   * valid a [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
   *
   * @param id the string typeid representation of the [RawId]
   * @return the result of the parsing as a [Validated] result
   */
  fun parseToValidatedRaw(id: String): Validated<RawId> {
    return factory.parseToValidatedRaw(id)
  }

  /**
   * Checks if the given string is a valid [Id] for the inferred entity type.
   *
   * @param TEntity the inferred entity type
   * @param id the string typeid representation of the [Id]
   * @return `true` if the string is a valid [Id], `false` otherwise
   */
  inline fun <reified TEntity> isId(id: String): Boolean {
    return isId(TEntity::class.java, id)
  }

  /**
   * Checks if the given string is a valid [Id] for the given entity type.
   *
   * @param TEntity the entity type
   * @param entityType the entity type class
   * @param id the string typeid representation of the [Id]
   * @return `true` if the string is a valid [Id], `false` otherwise
   */
  fun <TEntity> isId(entityType: Class<out TEntity>, id: String): Boolean {
    val validated = parseToValidated(entityType, id)
    return validated.map { true }.orElse(false)
  }

  /**
   * Sets a custom UUID generator for generating new UUIDs. This replaces the default UUIDv7
   * generator.
   *
   * Example:
   * ```
   * val typeId = TypeId()
   *  .withUUIDGenerator { Generators.randomBasedGenerator().generate() }
   * ```
   *
   * @param uuidGenerator the custom UUID generator supplier
   * @return the [TypeId] instance for chaining
   */
  fun withUUIDGenerator(uuidGenerator: Supplier<UUID>): TypeId {
    this.uuidGenerator = uuidGenerator
    return this
  }

  /**
   * Registers a custom prefix for the given entity type.
   *
   * Example:
   * ```
   * val typeId = TypeId()
   *  .withCustomPrefix(TypedPrefix<Organization>("org"))
   * ```
   *
   * @param TEntity the entity type
   * @param typedPrefix the typed prefix
   * @return the [TypeId] instance for chaining
   */
  inline fun <reified TEntity> withCustomPrefix(typedPrefix: TypedPrefix<out TEntity>): TypeId {
    factory.registerCustomPrefix(TEntity::class.java, typedPrefix)
    return this
  }

  /**
   * Registers a custom prefix for the given entity type.
   *
   * Example:
   * ```
   * val typeId = TypeId()
   *  .withCustomPrefix(Organization::class.java, TypedPrefix<Organization>("org"))
   * ```
   *
   * @param TEntity the entity type
   * @param typedPrefix the typed prefix
   * @return the [TypeId] instance for chaining
   */
  fun <TEntity> withCustomPrefix(
      entityType: Class<out TEntity>,
      typedPrefix: TypedPrefix<out TEntity>,
  ): TypeId {
    factory.registerCustomPrefix(entityType, typedPrefix)
    return this
  }

  /**
   * Creates a Jackson module for serializing and deserializing type-safe TypeId identifiers for
   * this instance of [TypeId].
   *
   * Usage:
   * ```
   * val typeid = typeId()
   * val objectMapper = ObjectMapper().registerModule(typeid.jacksonModule())
   * ```
   */
  fun jacksonModule() = TypeIdModule(this)

  companion object {
    /** The default UUID generator that generates time-based UUIDs. */
    val DEFAULT_UUID_GENERATOR = Supplier { Generators.timeBasedEpochGenerator().generate() }

    /**
     * The default [TypeId] instance with the default UUID generator. Used through the static
     * methods.
     */
    val DEFAULT = TypeId(DEFAULT_UUID_GENERATOR)

    /**
     * Creates a new random [Id] for the given entity type.
     *
     * Uses the default UUIDv7 generator for generating a new UUID.
     *
     * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
     * is derived from the lowercase simple class name of the entity type. For example, the prefix
     * for `User` is `user`.
     *
     * If you want to customize the prefix, use an explicitly created [TypeId] instance.
     *
     * @param TEntity the entity type
     * @return the new [Id]
     */
    inline fun <reified TEntity> randomId(): Id<out TEntity> {
      return DEFAULT.randomId()
    }

    /**
     * Creates a new random [Id] for the given entity type.
     *
     * Uses the default UUIDv7 generator for generating a new UUID.
     *
     * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
     * is derived from the lowercase simple class name of the entity type. For example, the prefix
     * for `User` is `user`.
     *
     * If you want to customize the prefix, use an explicitly created [TypeId] instance.
     *
     * @param entityType the inferred entity type class
     * @return the new [Id]
     */
    fun <TEntity> randomId(entityType: Class<out TEntity>): Id<out TEntity> {
      return DEFAULT.randomId(entityType)
    }

    /**
     * Creates a new random untyped [RawId] with the given prefix.
     *
     * Uses the default UUIDv7 generator for generating a new UUID.
     *
     * @param prefix the prefix of the identifier
     * @return the new [RawId]
     */
    fun randomId(prefix: String): RawId {
      return DEFAULT.randomId(prefix)
    }

    /**
     * Creates a new [Id] for the given entity type and UUID.
     *
     * Automatically infers the entity type and uses it to create the prefix. By default, the prefix
     * is derived from the lowercase simple class name of the entity type. For example, the prefix
     * for `User` is `user`.
     *
     * If you want to customize the prefix, use an explicitly created [TypeId] instance.
     *
     * @param TEntity the entity type
     * @param uuid the UUID
     * @return the new [Id]
     */
    inline fun <reified TEntity> of(uuid: UUID): Id<out TEntity> {
      return DEFAULT.of<TEntity>(uuid)
    }

    /**
     * Creates a new [Id] for the given entity type and UUID.
     *
     * By default, the prefix is derived from the lowercase simple class name of the entity type.
     * For example, the prefix for `User` is `user`.
     *
     * If you want to customize the prefix, use an explicitly created [TypeId] instance.
     *
     * @param TEntity the entity type
     * @param entityType the entity type class
     * @param uuid the UUID
     * @return the new [Id]
     */
    fun <TEntity> of(entityType: Class<out TEntity>, uuid: UUID): Id<out TEntity> {
      return DEFAULT.of(entityType, uuid)
    }

    /**
     * Creates a new untyped [RawId] with the given prefix and UUID.
     *
     * @param prefix the prefix of the identifier
     * @param uuid the UUID
     * @return the new [RawId]
     */
    fun of(prefix: String, uuid: UUID): RawId {
      return DEFAULT.of(prefix, uuid)
    }

    /**
     * Parses a typed [Id] from a string.
     *
     * @param TEntity the inferred entity type
     * @param id the string typeid representation of the [Id]
     * @return the parsed [Id]
     * @throws IllegalArgumentException if the string is not a valid [Id]
     */
    inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
      return DEFAULT.parse<TEntity>(id)
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
      return DEFAULT.parse(entityType, id)
    }

    /**
     * Parses an untyped [RawId] from a string.
     *
     * @param id the string typeid representation of the [RawId]
     * @return the parsed [RawId]
     * @throws IllegalArgumentException if the string is not a valid [RawId]
     */
    fun parse(id: String): RawId {
      return DEFAULT.parse(id)
    }

    /**
     * Parses a typed [Id] from a string and returns a [Validated] result. If the string is valid a
     * [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
     *
     * @param TEntity the inferred entity type
     * @param id the string typeid representation of the [Id]
     * @return the result of the parsing as a [Validated] result
     */
    inline fun <reified TEntity> parseToValidated(id: String): Validated<Id<out TEntity>> {
      return DEFAULT.parseToValidated<TEntity>(id)
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
      return DEFAULT.parseToValidated(entityType, id)
    }

    /**
     * Checks if the given string is a valid [Id] for the inferred entity type.
     *
     * @param TEntity the inferred entity type
     * @param id the string typeid representation of the [Id]
     * @return `true` if the string is a valid [Id], `false` otherwise
     */
    inline fun <reified TEntity> isId(id: String): Boolean {
      return DEFAULT.isId<TEntity>(id)
    }

    /**
     * Checks if the given string is a valid [Id] for the given entity type.
     *
     * @param TEntity the entity type
     * @param entityType the entity type class
     * @param id the string typeid representation of the [Id]
     * @return `true` if the string is a valid [Id], `false` otherwise
     */
    fun <TEntity> isId(entityType: Class<out TEntity>, id: String): Boolean {
      return DEFAULT.isId(entityType, id)
    }

    /**
     * Parses an untyped [RawId] from a string and returns a [Validated] result. If the string is
     * valid a [Validated.Valid] result is returned, otherwise it returns an [Validated.Invalid].
     *
     * @param id the string typeid representation of the [RawId]
     * @return the result of the parsing as a [Validated] result
     */
    fun parseToValidatedRaw(id: String): Validated<RawId> {
      return DEFAULT.parseToValidatedRaw(id)
    }

    /** Creates a Jackson module for serializing and deserializing type-safe TypeId identifiers. */
    fun jacksonModule() = DEFAULT.jacksonModule()
  }
}

/**
 * Factory function to create a new [TypeId] with the default UUID generator.
 *
 * Usage:
 * ```
 * val typeId = typeId()
 *    // Optionally customize the UUID generator
 *    .withUUIDGenerator { Generators.randomBasedGenerator().generate() }
 *    // Optionally register custom prefixes
 *    .withCustomPrefix(TypedPrefix<Organization>("org"))
 * ```
 */
fun typeId(): TypeId {
  return TypeId(TypeId.DEFAULT_UUID_GENERATOR)
}
