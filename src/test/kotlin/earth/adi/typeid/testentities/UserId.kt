package earth.adi.typeid.testentities

import earth.adi.typeid.Id

data class User(val id: UserId)

typealias UserId = Id<out User>
