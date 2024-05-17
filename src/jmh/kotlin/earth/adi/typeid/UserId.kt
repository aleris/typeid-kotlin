package earth.adi.typeid

data class User(val id: UserId)

typealias UserId = Id<out User>
