package earth.adi.typeid

import java.util.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole

open class TypeIdBenchmark {
  @Benchmark
  fun generate(bh: Blackhole) {
    bh.consume(TypeId.generate<User>())
  }

  @Benchmark
  fun of(bh: Blackhole, inputs: Inputs) {
    bh.consume(TypeId.of<User>(inputs.uuid))
  }

  @Benchmark
  fun toString(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.id.toString())
  }

  @Benchmark
  fun generateAndToString(bh: Blackhole, inputs: Inputs) {
    val id = inputs.typeId.generate<User>()
    bh.consume(id.toString())
  }

  @Benchmark
  fun ofAndToString(bh: Blackhole, inputs: Inputs) {
    val id = inputs.typeId.of<User>(inputs.uuid)
    bh.consume(id.toString())
  }

  @Benchmark
  fun parseSuccess(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parse<User>(inputs.validIdText))
  }

  @Benchmark
  fun parseError(bh: Blackhole, inputs: Inputs) {
    try {
      inputs.typeId.parse<User>(inputs.invalidIdText)
    } catch (e: Exception) {
      bh.consume(e)
    }
  }

  @Benchmark
  fun parseRawSuccess(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parse(inputs.validIdText))
  }

  @Benchmark
  fun parseRawError(bh: Blackhole, inputs: Inputs) {
    try {
      inputs.typeId.parse(inputs.invalidIdText)
    } catch (e: Exception) {
      bh.consume(e)
    }
  }

  @Benchmark
  fun parseToValidatedSuccess(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parseToValidated<User>(inputs.validIdText))
  }

  @Benchmark
  fun parseToValidatedError(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parseToValidated<User>(inputs.invalidIdText))
  }

  @Benchmark
  fun parseToValidatedRawSuccess(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parseToValidatedRaw(inputs.validIdText))
  }

  @Benchmark
  fun parseToValidatedRawError(bh: Blackhole, inputs: Inputs) {
    bh.consume(inputs.typeId.parseToValidatedRaw(inputs.invalidIdText))
  }

  @State(Scope.Benchmark)
  open class Inputs {
    lateinit var typeId: TypeId
    lateinit var uuid: UUID
    lateinit var validIdText: String
    lateinit var invalidIdText: String
    lateinit var id: UserId

    @Setup(Level.Trial)
    fun setup() {
      typeId = typeId()
      uuid = UUID.fromString("01890a5d-ac96-774b-bcce-b302099a8057")
      validIdText = "user_01h455vb4pex5vsknk084sn02q"
      invalidIdText = "user_01h455vb4pexÖvsknk084sn02q"
      id = TypeId.of<User>(uuid)
    }
  }
}
