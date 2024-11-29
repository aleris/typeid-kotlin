# typeid-kotlin
![Build Status](https://github.com/aleris/typeid-kotlin/actions/workflows/build-on-push.yml/badge.svg)
![Current Version](https://img.shields.io/badge/Version-1.0.1-blue)


## A Kotlin implementation of [TypeID](https://github.com/jetpack-io/typeid).

TypeIDs are a modern, type-safe, globally unique identifier based on the upcoming
UUIDv7 standard. They provide a ton of nice properties that make them a great choice
as the primary identifiers for your data in a database, APIs, and distributed systems.
Read more about TypeIDs in their [spec](https://github.com/jetpack-io/typeid).

Based on the Java implementation from [fxlae/typeid-java](https://github.com/fxlae/typeid-java).

This implementation adds a more complete type safety including id and their prefixes and uses an idiomatic Kotlin API.

[API Documentation](https://aleris.github.io/typeid-kotlin/)


## Dependency

To use with Maven:

```xml
<dependency>
    <groupId>earth.adi</groupId>
    <artifactId>typeid-kotlin</artifactId>
    <version>1.0.1</version>
</dependency>
```

To use via Gradle:

```kotlin
implementation("earth.adi:typeid-kotlin:1.0.1")
```


## Usage

The `TypeId` class is the main entry point for working with TypeIDs.
The class can be used to generate `Id` instances or parse them from strings.
They are typesafe, immutable and thread-safe.
 

### Generating ids

To use the typed features of the library, you need to define your typed id associated with an entity.

```kotlin
// Define your identifiable entity type:
data class User(val id: UserId) // can contain other fields

// Define a typealias for the user id.
typealias UserId = Id<out User>

```


#### generate

To generate a new `Id`, based on UUIDv7 as per specification:

```kotlin
// create a reusable TypeId instance, can be stored in a DI container
val typeId = typeId()

val userId: UserId = typeId.generate()
println(userId) // prints something like user_01h455vb4pex5vsknk084sn02q
println(typeId.typedPrefix.prefix) // "user"
println(typeId.uuid) // java.util.UUID(01890a5d-ac96-774b-bcce-b302099a8057)
```

This is inferring the Java type of the typeid to `UserId`.

Alternatively, specifying the entity type, for example for `User`,
will also generate the associated id `UserId`:

```kotlin
val userId = typeId.generate<User>()
```

Or specify the type explicitly, which can also be used from Java code as it does not rely on Kotlin type inference:

```kotlin
val userId = typeId.generate(User::class.java)
```

If the type of the id can be inferred, it will also work seamlessly:

```kotlin
data class User(val id: UserId)
val user = User(typeId.generate()) // infers UserId
```

Alternatively, directly use the static methods in `TypeId`:
    
```kotlin
val userId: UserId = TypeId.generate()
```

Using an explicit string prefix will instead generate a `RawId`:

```kotlin
val rawId: RawId = typeId.generate("custom")
println(rawId) // prints something like custom_01h455vb4pex5vsknk084sn02q
```

Raw ids are just a string with a prefix and a UUID, without any Java/Kotlin type information, 
so it is better to use typed ids whenever possible (see [Type safety](#type-safety) below).

All methods described below also have raw variants.


#### of

To construct (or reconstruct) an `Id` from an `UUID`:

```kotlin
val userId: UserId = typeId.of(someUUID)
```

or for a `RawId`:
```kotlin
val userId: RawId = TypeId.of("user", someUUID)
```


### Parsing from strings

For parsing, the library supports both an imperative programming model and a more functional style.


#### parse

The most straightforward way to parse the textual representation of an id:


```kotlin
val userId: UserId = typeId.parse("user_01h455vb4pex5vsknk084sn02q")

```

Invalid inputs will result in an `IllegalArgumentException`, with a message explaining the cause of the parsing failure.

To parse a `RawId`:

```kotlin
val rawId: RawId = TypeId.parse("custom_01h455vb4pex5vsknk084sn02q")
```

Will create a `RawId` instance with the prefix 'custom'.


#### parseToValidated

If you prefer working with errors modeled as return values rather than exceptions, 
this is also possible (and is *much* more performant for untrusted input with high error rates, 
as no stacktrace is involved):


```kotlin
val userId: UserId = typeId.parseToValidated("user_01h455vb4pex5vsknk084sn02q")

when(userId) {
    is Validated.Valid -> {
        val userId = userId.id
        // Proceed with userId
    }
    is Validated.Invalid -> {
        val error = userId.error
        // Optionally, do something with the error message
    }
}
```

The `Validated` class includes a couple of functional style helper methods like `filter` and `map`.

Example:

```kotlin
typeId
  .parseToValidated<User>("user_01h455vb4pex5vsknk084sn02q")
  .filter { it.id == idFromSomewhereElse }
  .map { it.id }
  .ifValid { println("Valid id: $it") }
```

Another safe alternative for working with validated is to use Kotlin functions like:

```kotlin
val id = typeId.parseToValidated<User>("user_01h455vb4pex5vsknk084sn02q")
  .takeIf { it is Validated.Valid }
  ?.let { it as Validated.Valid }
  ?.id
if (id != null) {
  println("Valid id: $id")
}
```

These approaches are much faster when the input is untrusted and can result in lots of exceptions otherwise 
(see [Benchmarks](#benchmarks)).


### isId

Check if a string is a valid id of the given type:

```kotlin
val isUserId = typeId.isId<UserId>("user_01h455vb4pex5vsknk084sn02q")
```

Is a convenience method that uses `parseToValidated` and returns a boolean.
It can be used to check if a string is a valid id of an expected type 
as it returns `false` if the id is valid but of a different type.


### Type safety

At its base, a `typeid` is just a prefix followed by `_` and an encoded UUID 
(see the [spec](https://github.com/jetify-com/typeid/tree/main/spec)).
After it is encoded is just a string.

This could result in bugs if you accidentally mix up ids from different entities.

```kotlin
val id: RawId = typeId.generate("user")

// ... sometime later
val orgExists = someService.checkIfOrganizationExists(id)
// returns false most of the time so the bug may be hard to find
```

The library provides a type-safe way to work with these ids, by associating them with a specific type.

1. Fail if unexpected prefix is used
```kotlin
// fails if id does not have a `user` prefix
val userId: UserId = typeId.parse(id) 
```

2. Compile time safety
```kotlin
val id: UserId = typeId.parse(text)

// ... sometime later
val orgExists = someService.checkIfOrganizationExists(id)
// compile error, as id is of type `Id<User>` (or `UserId` if using a typealias), 
// not Id<Organization>
```


### Customizing prefixes

The `TypeId` class can be customized to use a specific prefix for the generated ids 
associated with an entity type.

For example to register a custom prefix for the `Organization` entity:
```kotlin
val typeId = typeId().withCustomPrefix(TypedPrefix<Organization>("org"))
println(typeId.generate<Organization>()) // prints something like org_01h455vb4pex5vsknk084sn02q
```

Another possibility is to add the `TypedPrefix` annotation to the entity instance:

```kotlin
@TypeIdPrefix("cust")
data class Customer(override val id: CustomerId)
```

This can also be useful when you want a different entity interface (maybe defined in a different module). 
For example, define an interface with the `@TypeIdPrefix` annotation,
which is implemented by the entity class:  

```kotlin
@TypeIdPrefix("cust")
interface CustomerIdentifiable {
  val id: CustomerId
}

typealias CustomerId = Id<out CustomerIdentifiable>

data class Customer(override val id: CustomerId) : CustomerIdentifiable
```

If the `@TypeIdPrefix` is present (on the entity or one of its interfaces) TypeId will use that. 
Note that the prefixes registered through the `TypeId` instance will take precedence 
over the ones defined with annotations, you should use just one of the two methods to define prefixes.


### Customizing the UUID generator

By default, the library uses the `UUIDv7` generator, as per typeid specification,
but you can provide your own generator.

```kotlin
// use Java UUID random generator
val typeId = typeId().withUUIDGenerator { UUID.randomUUID() }
// or using com.fasterxml.uuid:java-uuid-generator
val typeId = typeId().withUUIDGenerator { Generators.randomBasedGenerator().generate() }
```


### Serialization and deserialization

The ids in this library have built-in serialization and deserialization support
for Java, Kotlin (kotlinx.serialization), and Jackson.


#### Kotlin (kotlinx.serialization)

Both `Id` and `RawId` have `@Serializable` and can be used with `kotlinx.serialization`.
You need to include the actual serialization dependency in your project.

For example, with CBOR:

`include("io.github.microutils:kotlin-serialization-cbor:1.6.3")`

```kotlin
val bytes = Cbor.encodeToByteArray<Id<User>>(id)
val deserialized = Cbor.decodeFromByteArray<Id<User>>(bytes)
```


#### Jackson

The library provides a Jackson module to serialize and deserialize `Id` instances.

```kotlin
private val objectMapper = jacksonObjectMapper().registerModule(typeId.jacksonModule())

data class UserAndOrganization(
  val user: User,
  val organization: Organization,
)

val userAndOrganization =
  UserAndOrganization(
    User(typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh")),
    Organization(typeId.parse<Organization>("org_01hy0sk45qfmdsdme1j703yjet")),
  )
val writtenJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(userAndOrganization)
// writes:
// {
//    "user" : {
//      "id" : "user_01hy0d96sgfx0rh975kqkspchh"
//    },
//    "organization" : {
//      "id" : "org_01hy0sk45qfmdsdme1j703yjet"
//    }
// }

val read = objectMapper.readValue<JsonUserAndOrganization>(writtenJson)
// read.user.id is same as typeId.parse<User>("user_01hy0d96sgfx0rh975kqkspchh")
```


## Using it with Spring

See [Spring Snippets](https://github.com/aleris/typeid-kotlin/wiki/Using-kotlin-TypeId-type‐safe-ids-with-Spring) 
for examples on how to use `TypeId` with Spring Data and WebMvc by creating converters and formatters.


## Building From Source
 <details>
    <summary>Details</summary>

```console
~$ git clone https://github.com/aleris/typeid-kotlin.git
~$ cd typeid-kotling
~/typeid-kotlin sdk use java 17.0.9-tem
~/typeid-kotlin ./gradlew build
```
</details>


## Releasing
 <details>
    <summary>Details</summary>

```console
~$ cd typeid-kotling
# Update version in build.gradle.kts
~/typeid-kotlin ./gradlew updateReadmeVersion # updates the version in README.md from build.gradle.kts
~/typeid-kotlin ./gradlew jreleaserConfig # just to double check the configuration
~/typeid-kotlin ./gradlew clean
~/typeid-kotlin ./gradlew publish
~/typeid-kotlin ./gradlew jreleaserFullRelease
```
</details>


## Benchmarks
<details>
    <summary>Details</summary>

There is a small [JMH](https://github.com/openjdk/jmh) microbenchmark included:
```console
~/typeid-kotlin ./gradlew jmh
```

In a single-threaded run, all operations perform in the range of millions of calls per second,
which should be enough for most use cases 
(used setup: Eclipse Temurin 17 JDK, 2021 MacBook Pro, run on version 1.0.0).


| Benchmark                       | Mode  | Cnt |          Score |            Error | Units |
|---------------------------------|-------|----:|---------------:|-----------------:|-------|
| `generate`                      | thrpt |   4 |  2.785.895,675 | ±    791.836,864 | ops/s |
| `generate` + `toString`         | thrpt |   4 |  2.060.627,959 | ±  1.185.777,089 | ops/s |
| `of`                            | thrpt |   4 | 20.084.528,045 | ± 3.3543.123,085 | ops/s |
| `of` + `toString`               | thrpt |   4 |  5.853.485,485 | ±   1262.620,609 | ops/s |
| `parse` (Error)                 | thrpt |   4 |    862.446,936 | ±     63.583,514 | ops/s |
| `parse`  (Success)              | thrpt |   4 |  9.335.663,639 | ±    733.015,389 | ops/s |
| `parseRaw` (Error)              | thrpt |   4 |    841.795,541 | ±    143.272,942 | ops/s |
| `parseRaw` (Success)            | thrpt |   4 | 13.555.610,086 | ±  5.390.579,926 | ops/s |
| `parseToValidated` (Error)      | thrpt |   4 | 20.242.071,304 | ±  2.514.786,867 | ops/s |
| `parseToValidated` (Success)    | thrpt |   4 |  7.145.891,307 | ±  8.080.357,687 | ops/s |
| `parseToValidatedRaw` (Error)   | thrpt |   4 | 39.712.927,570 | ±  8.692.614,496 | ops/s |
| `parseToValidatedRaw` (Success) | thrpt |   4 | 12.377.605,683 | ±  4.873.224,352 | ops/s |
| `toString`                      | thrpt |   4 |  9.783.700,478 | ±  2.152.238,948 | ops/s |

</details>
