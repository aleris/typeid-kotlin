package earth.adi.typeid

import earth.adi.typeid.testentities.*
import java.util.*
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class TypeIdTest {
  @Test
  fun `default generate with inference of entity`() {
    val userId = TypeId.generate<User>() // user_01hy0d96sgfx0rh975kqkspchh
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default generate with inference of id`() {
    val userId: UserId = TypeId.generate() // user_01hy0d96sgfx0rh975kqkspchh
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default generate with entity type`() {
    val userId = TypeId.generate(User::class.java)
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default generate raw`() {
    val userId = TypeId.generate("user")
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default of`() {
    val userId = TypeId.of<User>(UUID.fromString("00000000-0000-0000-0000-000000000000"))
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default of with entity type`() {
    val userId =
        TypeId.of(User::class.java, UUID.fromString("00000000-0000-0000-0000-000000000000"))
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default of raw`() {
    val userId = TypeId.of("user", UUID.fromString("00000000-0000-0000-0000-000000000000"))
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `default parse valid`() {
    val userId = TypeId.generate<User>()
    val parsedUserId = TypeId.parse<User>(userId.toString())
    assertThat(parsedUserId).isEqualTo(userId)
  }

  @Test
  fun `default parse invalid`() {
    assertThatThrownBy { TypeId.parse<User>("_") }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `default parse with entity type`() {
    val userId = TypeId.generate<User>()
    val parsedUserId = TypeId.parse(User::class.java, userId.toString())
    assertThat(parsedUserId).isEqualTo(userId)
  }

  @Test
  fun `default parse raw valid`() {
    val userId = TypeId.generate("user")
    val parsedUserId = TypeId.parse(userId.toString())
    assertThat(parsedUserId).isEqualTo(userId)
  }

  @Test
  fun `default parse raw invalid`() {
    assertThatThrownBy { TypeId.parse("_") }.isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `default parseToValidated valid`() {
    val userId = TypeId.generate<User>()
    val parsedUserId = TypeId.parseToValidated<User>(userId.toString())
    assertThat(parsedUserId).isEqualTo(Validated.Valid(userId))
  }

  @Test
  fun `default parseToValidated invalid`() {
    val parsedUserId = TypeId.parseToValidated<User>("_")
    assertThat(parsedUserId).isInstanceOf(Validated.Invalid::class.java)
    assertThat(parsedUserId)
        .isEqualTo(
            Validated.Invalid<User>("Id with empty prefix must not contain the separator '_'"))
  }

  @Test
  fun `default isId true`() {
    val organizationId = TypeId.generate<Organization>()
    assertThat(TypeId.isId<Organization>(organizationId.toString())).isTrue()
  }

  @Test
  fun `default isId true with entity type`() {
    val organizationId = TypeId.generate<Organization>()
    assertThat(TypeId.isId(Organization::class.java, organizationId.toString())).isTrue()
  }

  @Test
  fun `default isId false`() {
    val organizationId = TypeId.generate<Organization>()
    assertThat(TypeId.isId<User>(organizationId.toString())).isFalse()
  }

  @Test
  fun `default parseToValidated with entity type`() {
    val userId = TypeId.generate<User>()
    val parsedUserId = TypeId.parseToValidated(User::class.java, userId.toString())
    assertThat(parsedUserId).isEqualTo(Validated.Valid(userId))
  }

  @Test
  fun `default parseToValidatedRaw valid`() {
    val userId = TypeId.generate("user")
    val parsedUserId = TypeId.parseToValidatedRaw(userId.toString())
    assertThat(parsedUserId).isEqualTo(Validated.Valid(userId))
  }

  @Test
  fun `default parseToValidatedRaw invalid`() {
    val parsedUserId = TypeId.parseToValidatedRaw("_")
    assertThat(parsedUserId).isInstanceOf(Validated.Invalid::class.java)
    assertThat(parsedUserId)
        .isEqualTo(
            Validated.Invalid<User>("Id with empty prefix must not contain the separator '_'"))
  }

  // The instance keeps a cache of prefixes so, where performance matters, create it only once
  private val typeId = typeId().withCustomPrefix(TypedPrefix<Organization>("org"))

  @Test
  fun `generate with default prefix`() {
    val userId = typeId.generate<User>()
    assertThat(userId.toString()).startsWith("user_")
  }

  @Test
  fun `generate with prefix customization`() {
    val organizationId = typeId.generate<Organization>() // org_01hy0sk45qfmdsdme1j703yjet
    assertThat(organizationId.toString()).startsWith("org_")
  }

  @Test
  fun `generate with prefix customization in annotation`() {
    val customerId = typeId.generate<CustomerIdentifiable>() // customer_01hy0sk45qfmdsdme1j703yjet
    assertThat(customerId.toString()).startsWith("cust_")
  }

  @Test
  fun `generate with prefix customization in annotation from interface`() {
    val customerId = typeId.generate<Customer>() // customer_01hy0sk45qfmdsdme1j703yjet
    assertThat(customerId.toString()).startsWith("cust_")
  }

  interface EntityIdentifiable

  data class Entity(val id: Id<Entity>) : EntityIdentifiable

  @Test
  fun `for class with interface without interface`() {
    val customerId = typeId.generate<Entity>()
    assertThat(customerId.toString()).startsWith("entity_")
  }

  @Test
  fun `parse valid`() {
    val organizationId = typeId.generate<Organization>()
    val parsedUserId = typeId.parse<Organization>(organizationId.toString())
    assertThat(parsedUserId).isEqualTo(organizationId)
  }

  @Test
  fun `parse invalid`() {
    assertThatThrownBy { typeId.parse<Organization>("_") }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `parseToValidated valid`() {
    val organizationId = typeId.generate<Organization>()
    val parsedOrganizationId = typeId.parseToValidated<Organization>(organizationId.toString())
    assertThat(parsedOrganizationId).isInstanceOf(Validated.Valid::class.java)
    assertThat(parsedOrganizationId).isEqualTo(Validated.Valid(organizationId))
  }

  @Test
  fun `parseToValidated valid with entity type`() {
    val organizationId = typeId.generate<Organization>()
    val parsedOrganizationId =
        typeId.parseToValidated(Organization::class.java, organizationId.toString())
    assertThat(parsedOrganizationId).isInstanceOf(Validated.Valid::class.java)
    assertThat(parsedOrganizationId).isEqualTo(Validated.Valid(organizationId))
  }

  @Test
  fun `parseToValidated invalid`() {
    val parsedOrganizationId = typeId.parseToValidated<Organization>("_")
    assertThat(parsedOrganizationId).isInstanceOf(Validated.Invalid::class.java)
    assertThat(parsedOrganizationId)
        .isEqualTo(
            Validated.Invalid<Organization>(
                "Id with empty prefix must not contain the separator '_'"))
  }

  @Test
  fun `isId true`() {
    val organizationId = typeId.generate<Organization>()
    assertThat(typeId.isId<Organization>(organizationId.toString())).isTrue()
  }

  @Test
  fun `isId true with entity type`() {
    val organizationId = typeId.generate<Organization>()
    assertThat(typeId.isId(Organization::class.java, organizationId.toString())).isTrue()
  }

  @Test
  fun `isId false for other type`() {
    val organizationId = typeId.generate<Organization>()
    assertThat(typeId.isId<User>(organizationId.toString())).isFalse()
  }

  @Test
  fun `isId false for other type with entitytype`() {
    val organizationId = typeId.generate<Organization>()
    assertThat(typeId.isId(User::class.java, organizationId.toString())).isFalse()
  }

  @Test
  fun `isId false for invalid`() {
    assertThat(typeId.isId<User>("user_not_valid_id")).isFalse()
  }

  @Test
  fun `type safe parsing`() {
    val userId = typeId.generate<User>()
    val userIdString = userId.toString() // user_...
    assertThatThrownBy {
          TypeId.parse<Organization>(userIdString) // must have prefix 'org'
        }
        .isInstanceOf(IllegalArgumentException::class.java)
  }

  @Test
  fun `type safe usage`() {
    val userRepository = UserRepository()

    // val organizationId =
    // typeId.of<Organization>(UUID.fromString("00000000-0000-0000-0000-000000000001"))
    // compiler error: Required: UserId, Found: Id<out Organization>
    // userRepository.get(organizationId)

    val userId = typeId.of<User>(UUID.fromString("00000000-0000-0000-0000-000000000002"))
    // ok with type safe usage
    assertThat(userRepository.get(userId)).isInstanceOf(User::class.java)
  }

  @Test
  fun `withCustomPrefix with entity type`() {
    val typeId = typeId().withCustomPrefix(Organization::class.java, TypedPrefix("org"))
    val organizationId = typeId.generate<Organization>()
    assertThat(organizationId.toString()).startsWith("org_")
  }

  @Test
  fun `parseToValidated with entity type`() {
    val organizationId =
        typeId.parseToValidated(Organization::class.java, "org_01hy0sk45qfmdsdme1j703yjet")
    assertThat(organizationId).isInstanceOf(Validated.Valid::class.java)
  }

  @Test
  fun `withUUIDGenerator non default`() {
    val uuid = UUID.fromString("00000000-0000-0000-0000-000000000000")
    val typeId = typeId().withUUIDGenerator { uuid }
    val organizationId = typeId.generate<Organization>()
    assertThat(organizationId.uuid).isEqualTo(uuid)
  }
}
