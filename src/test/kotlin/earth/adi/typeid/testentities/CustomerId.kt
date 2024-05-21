package earth.adi.typeid.testentities

import earth.adi.typeid.Id
import earth.adi.typeid.annotations.TypeIdPrefix

@TypeIdPrefix("cust")
interface CustomerIdentifiable {
  val id: CustomerId
}

typealias CustomerId = Id<out CustomerIdentifiable>

data class Customer(override val id: CustomerId) : CustomerIdentifiable
