package earth.adi.typeid

import com.fasterxml.uuid.Generators
import earth.adi.typeid.codec.Factory
import earth.adi.typeid.jackson.TypeIdModule
import java.util.*
import java.util.function.Supplier

class TypeId(private var uuidGenerator: Supplier<UUID>) {
  val factory = Factory()

  inline fun <reified TEntity> randomId(): Id<out TEntity> {
    return randomId(TEntity::class.java)
  }

  fun <TEntity> randomId(entityType: Class<out TEntity>): Id<out TEntity> {
    return factory.create(entityType, uuidGenerator.get())
  }

  inline fun <reified TEntity> of(uuid: UUID): Id<out TEntity> {
    return factory.create(uuid)
  }

  fun <TEntity> of(entityType: Class<out TEntity>, uuid: UUID): Id<out TEntity> {
    return factory.create(entityType, uuid)
  }

  inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
    return factory.parse<TEntity>(id)
  }

  fun <TEntity> parse(entityType: Class<out TEntity>, id: String): Id<out TEntity> {
    return factory.parse(entityType, id)
  }

  inline fun <reified TEntity> parseToValidated(id: String): Validated<out TEntity> {
    return factory.parseToValidated<TEntity>(id)
  }

  fun <TEntity> parseToValidated(
      entityType: Class<out TEntity>,
      id: String
  ): Validated<out TEntity> {
    return factory.parseToValidated(entityType, id)
  }

  fun withUUIDGenerator(uuidGenerator: Supplier<UUID>): TypeId {
    this.uuidGenerator = uuidGenerator
    return this
  }

  inline fun <reified TEntity> withCustomPrefix(typedPrefix: TypedPrefix<out TEntity>): TypeId {
    factory.registerCustomPrefix(TEntity::class.java, typedPrefix)
    return this
  }

  fun <TEntity> withCustomPrefix(
      entityType: Class<out TEntity>,
      typedPrefix: TypedPrefix<out TEntity>,
  ): TypeId {
    factory.registerCustomPrefix(entityType, typedPrefix)
    return this
  }

  fun jacksonModule() = TypeIdModule(this)

  companion object {
    val DEFAULT_UUID_GENERATOR = Supplier { Generators.timeBasedEpochGenerator().generate() }
    val DEFAULT = TypeId(DEFAULT_UUID_GENERATOR)

    inline fun <reified TEntity> randomId(): Id<out TEntity> {
      return DEFAULT.randomId()
    }

    fun <TEntity> randomId(entityType: Class<out TEntity>): Id<out TEntity> {
      return DEFAULT.randomId(entityType)
    }

    inline fun <reified TEntity> of(uuid: UUID): Id<out TEntity> {
      return DEFAULT.of<TEntity>(uuid)
    }

    fun <TEntity> of(entityType: Class<out TEntity>, uuid: UUID): Id<out TEntity> {
      return DEFAULT.of(entityType, uuid)
    }

    inline fun <reified TEntity> parse(id: String): Id<out TEntity> {
      return DEFAULT.parse<TEntity>(id)
    }

    fun <TEntity> parse(entityType: Class<out TEntity>, id: String): Id<out TEntity> {
      return DEFAULT.parse(entityType, id)
    }

    inline fun <reified TEntity> parseToValidated(id: String): Validated<out TEntity> {
      return DEFAULT.parseToValidated<TEntity>(id)
    }

    fun <TEntity> parseToValidated(
        entityType: Class<out TEntity>,
        id: String
    ): Validated<out TEntity> {
      return DEFAULT.parseToValidated(entityType, id)
    }

    fun jacksonModule() = DEFAULT.jacksonModule()
  }
}

/**
 * Creates a new [TypeId] with the default UUID generator.
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
