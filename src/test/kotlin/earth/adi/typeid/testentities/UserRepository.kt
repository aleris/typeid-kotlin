package earth.adi.typeid.testentities

class UserRepository {
  fun get(id: UserId): User {
    return User(id)
  }
}
