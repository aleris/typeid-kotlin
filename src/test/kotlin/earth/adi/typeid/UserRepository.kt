package earth.adi.typeid

class UserRepository {
  fun get(id: UserId): User {
    return User(id)
  }
}
