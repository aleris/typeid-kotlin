package earth.adi.typeid

sealed class Validated<TEntity> {
  data class Valid<TEntity>(val id: Id<TEntity>) : Validated<TEntity>()

  data class Invalid<TEntity>(val error: String) : Validated<TEntity>()
}
