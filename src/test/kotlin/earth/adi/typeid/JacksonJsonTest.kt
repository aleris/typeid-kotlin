package earth.adi.typeid

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.PropertyName
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import earth.adi.typeid.jackson.IdJsonDeserializer
import earth.adi.typeid.jackson.IdJsonSerializer
import earth.adi.typeid.jackson.RawIdJsonSerializer
import java.io.ByteArrayOutputStream
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class JacksonJsonTest {
  private val defaultObjectMapper = jacksonObjectMapper().registerModule(TypeId.jacksonModule())

  @Test
  fun `default deserialize from json`() {
    val user = defaultObjectMapper.readValue<User>(JSON_USER)
    assertThat(user.id).isEqualTo(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh"))
  }

  @Test
  fun `default serialize to json`() {
    val user = User(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh"))
    val writtenJson = defaultObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(user)
    assertThat(writtenJson).isEqualTo(JSON_USER)
  }

  private val typeId = typeId().withCustomPrefix(TypedPrefix<Organization>("org"))

  private val objectMapper = jacksonObjectMapper().registerModule(typeId.jacksonModule())

  data class JsonUserAndOrganization(
      val user: User,
      val organization: Organization,
  )

  @Test
  fun `deserialize from json`() {
    val json = objectMapper.readValue<JsonUserAndOrganization>(JSON_USER_AND_ORGANIZATION)
    assertThat(json.user.id).isEqualTo(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh"))
    assertThat(json.organization.id)
        .isEqualTo(typeId.parse<Organization>("org_01hy0sk45qfmdsdme1j703yjet"))
  }

  @Test
  fun `serialize to json`() {
    val json =
        JsonUserAndOrganization(
            User(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh")),
            Organization(typeId.parse<Organization>("org_01hy0sk45qfmdsdme1j703yjet")),
        )
    val writtenJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
    assertThat(writtenJson).isEqualTo(JSON_USER_AND_ORGANIZATION)
  }

  data class IdsOnly(
      @JsonProperty("user_id") val userId: UserId,
      @JsonProperty("org_id") val organizationId: OrganizationId,
  )

  @Test
  fun `deserialize ids only from json`() {
    val json = objectMapper.readValue<IdsOnly>(JSON_IDS_ONLY)
    assertThat(json.userId).isEqualTo(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh"))
    assertThat(json.organizationId)
        .isEqualTo(typeId.parse<Organization>("org_01hy0sk45qfmdsdme1j703yjet"))
  }

  @Test
  fun `serialize ids only to json`() {
    val json =
        IdsOnly(
            typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh"),
            typeId.parse<Organization>("org_01hy0sk45qfmdsdme1j703yjet"),
        )
    val writtenJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
    assertThat(writtenJson).isEqualTo(JSON_IDS_ONLY)
  }

  data class RawIds(
      @JsonProperty("user_id") val userId: RawId,
      @JsonProperty("org_id") val organizationId: RawId,
  )

  @Test
  fun `deserialize raw ids from json`() {
    val json = objectMapper.readValue<RawIds>(JSON_IDS_ONLY)
    assertThat(json.userId).isEqualTo(typeId.parse("user_01hy0d96sgfx0rh975kqkspchh"))
    assertThat(json.organizationId).isEqualTo(typeId.parse("org_01hy0sk45qfmdsdme1j703yjet"))
  }

  @Test
  fun `serialize raw ids to json`() {
    val json =
        RawIds(
            typeId.parse("user_01hy0d96sgfx0rh975kqkspchh"),
            typeId.parse("org_01hy0sk45qfmdsdme1j703yjet"),
        )
    val writtenJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(json)
    assertThat(writtenJson).isEqualTo(JSON_IDS_ONLY)
  }

  @Test
  fun `create IdJsonDeserializer null deserializerContext`() {
    val type = objectMapper.constructType(Id::class.java)
    val beanProperty = BeanProperty.Std(PropertyName.NO_NAME, type, null, null, null)
    val idJsonDeserializer =
        IdJsonDeserializer(typeId()).createContextual(null, beanProperty) as IdJsonDeserializer
    assertThat(idJsonDeserializer.valueType).isEqualTo(type)
  }

  @Test
  fun `create IdJsonDeserializer null deserializerContext and no beanProperty type`() {
    val beanProperty = BeanProperty.Bogus()
    val idJsonDeserializer =
        IdJsonDeserializer(typeId()).createContextual(null, beanProperty) as IdJsonDeserializer
    assertThat(idJsonDeserializer.valueType).isNotNull
  }

  @Test
  fun `create IdJsonDeserializer null deserializerContext and beanProperty null`() {
    val idJsonDeserializer =
        IdJsonDeserializer(typeId()).createContextual(null, null) as IdJsonDeserializer
    assertThat(idJsonDeserializer.valueType).isNull()
  }

  @Test
  fun `IdJsonSerializer null id`() {
    ByteArrayOutputStream().use { outputStream ->
      IdJsonSerializer().serialize(null, objectMapper.createGenerator(outputStream), null)
      assertThat(outputStream.toString()).isEqualTo("")
    }
  }

  @Test
  fun `RawIdJsonSerializer null id`() {
    ByteArrayOutputStream().use { outputStream ->
      RawIdJsonSerializer().serialize(null, objectMapper.createGenerator(outputStream), null)
      assertThat(outputStream.toString()).isEqualTo("")
    }
  }

  companion object {
    private val JSON_USER =
        """
      {
        "id" : "user_01hy0d96sgfx0rh975kqkspchh"
      }
      """
            .trimIndent()

    private val JSON_USER_AND_ORGANIZATION =
        """
      {
        "user" : {
          "id" : "user_01hy0d96sgfx0rh975kqkspchh"
        },
        "organization" : {
          "id" : "org_01hy0sk45qfmdsdme1j703yjet"
        }
      }
      """
            .trimIndent()

    private val JSON_IDS_ONLY =
        """
      {
        "user_id" : "user_01hy0d96sgfx0rh975kqkspchh",
        "org_id" : "org_01hy0sk45qfmdsdme1j703yjet"
      }
      """
            .trimIndent()
  }
}
