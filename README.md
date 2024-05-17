# typeid-kotlin
![Build Status](https://github.com/aleris/typeid-kotlin/actions/workflows/build-on-push.yml/badge.svg)


## A Kotlin implementation of [TypeID](https://github.com/jetpack-io/typeid).

TypeIDs are a modern, type-safe, globally unique identifier based on the upcoming
UUIDv7 standard. They provide a ton of nice properties that make them a great choice
as the primary identifiers for your data in a database, APIs, and distributed systems.
Read more about TypeIDs in their [spec](https://github.com/jetpack-io/typeid).

Based on the Java implementation from [typeid-java](https://github.com/fxlae/typeid-java).

This implementation adds a more complete type safety including id and their prefixes and uses an idiomatic Kotlin API.


## Dependency

To use with Maven:

```xml
<dependency>
    <groupId>earth.adi</groupId>
    <artifactId>typeid-kotlin</artifactId>
    <version>0.0.1</version>
</dependency>
```

To use via Gradle:

```kotlin
implementation("earth.adi:typeid-kotlin:0.0.1")
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

#### randomId

To generate a new `Id`, based on UUIDv7 as per specification:

```kotlin
// create a reusable TypeId instance, can be stored in a DI container
val typeId = typeId()

val userId = typeId.randomId<User>()
println(userId) // prints something like user_01h455vb4pex5vsknk084sn02q
println(typeId.typedPrefix.prefix) // "user"
println(typeId.uuid) // java.util.UUID(01890a5d-ac96-774b-bcce-b302099a8057)
```

Alternatively, if you do not want to customize prefixes, directly use the static methods in `TypeId`:
    
```kotlin
val userId = TypeId.randomId<User>()
```

#### of

To construct (or reconstruct) an `Id` from an `UUID`:

```kotlin
val userId = typeId.of<User>(someUUID)
```

### Parsing from strings

For parsing, the library supports both an imperative programming model and a more functional style.


#### parse

The most straightforward way to parse the textual representation of an id:


```kotlin
val userId = typeId.parse<User>("user_01h455vb4pex5vsknk084sn02q")

```

Invalid inputs will result in an `IllegalArgumentException`, with a message explaining the cause of the parsing failure.


#### parseToValidated

If you prefer working with errors modeled as return values rather than exceptions, 
this is also possible (and is *much* more performant for untrusted input with high error rates, 
as no stacktrace is involved):


```kotlin
val validated = typeId.parseToValidated<User>("user_01h455vb4pex5vsknk084sn02q")

when(validated) {
    is Validated.Valid -> {
        val userId = validated.id
        // Proceed with userId
    }
    is Validated.Invalid -> {
        val error = validated.error
        // Optionally, do something with the error message
    }
}
```

Another safe alternative for working with validated is to use Kotlin functions like:

```kotlin
val id = typeId.parseToValidated<User>("user_01h455vb4pex5vsknk084sn02q")
  .takeIf { it is Validated.Valid }
  ?.let { it as Validated.Valid }
  ?.id
```


### Customizing prefixes

The `TypeId` class can be customized to use a different prefix for the generated ids.

```kotlin
val typeId = typeId().withCustomPrefix(TypedPrefix<Organization>("org"))
println(typeId.randomId<Organization>()) // prints something like org_01h455vb4pex5vsknk084sn02q
```

### Customizing UUID generator

By default, the library uses the `UUIDv7` generator, but you can provide your own generator.

```kotlin
// use Java UUID random generator
val typeId = typeId().withUUIDGenerator { UUID.randomUUID() }
// or using com.fasterxml.uuid:java-uuid-generator
val typeId = typeId().withUUIDGenerator { Generators.randomBasedGenerator().generate() }
```


### JSON serialization and deserialization

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


## Building From Source & Benchmarks
 <details>
    <summary>Details</summary>

```console
~$ git clone https://github.com/aleris/typeid-kotlin.git
~$ cd typeid-kotling
~/typeid-kotlin ./gradlew build
```

There is a small [JMH](https://github.com/openjdk/jmh) microbenchmark included:
```console
~/typeid-kotlin ./gradlew jmh
```

In a single-threaded run, all operations perform in the range of millions of calls per second, 
which should be enough for most use cases (used setup: Eclipse Temurin 17 JDK, 2021 MacBook Pro).

| Benchmark                           | Mode  | Cnt |          Score |           Error | Units |
|-------------------------------------|-------|----:|---------------:|----------------:|-------|
| `TypeId.of`                         | thrpt |   4 | 19.517.016,660 |  ±  697.279,162 | ops/s |
| `TypeId.of` + `toString`            | thrpt |   4 |  5.929.486,771 |  ±  696.977,896 | ops/s |
| `TypeId.parse` (Error)              | thrpt |   4 |    786.626,228 |  ±  258.964,881 | ops/s |
| `TypeId.parse` (Success)            | thrpt |   4 |  9.039.244,704 | ± 1.697.354,215 | ops/s |
| `TypeId.parseToValidated` (Error)   | thrpt |   4 | 19.665.449,579 | ± 3.682.665,039 | ops/s |
| `TypeId.parseToValidated` (Success) | thrpt |   4 |  8.737.722,345 |  ±  303.399,494 | ops/s |
| `TypeId.randomId`                   | thrpt |   4 |  2.759.591,306 |  ±  507.771,074 | ops/s |
| `TypeId.randomId` + `toString`      | thrpt |   4 |  2.103.888,015 |  ±   74.446,935 | ops/s |
| `TypeId.toString`                   | thrpt |   4 |  9.631.430,676 |  ±  934.174,210 | ops/s |

</details>
