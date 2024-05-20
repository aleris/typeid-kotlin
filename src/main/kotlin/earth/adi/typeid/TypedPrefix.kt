package earth.adi.typeid

import java.io.Serializable

/** Typed prefix for a type id. */
data class TypedPrefix<TEntity>(val prefix: String) : Serializable
