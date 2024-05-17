import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.spotless)
  jacoco
  alias(libs.plugins.jmh)
}

group = "earth.adi"

version = "0.0.3"

repositories { mavenCentral() }

dependencies {
  implementation(libs.javaUuidGenerator)
  implementation(platform(libs.jacksonBom))
  implementation(libs.bundles.jackson)

  testImplementation(platform(libs.junitBom))
  testImplementation(libs.bundles.test)
}

kotlin { jvmToolchain(17) }

java { withSourcesJar() }

tasks.withType<JavaCompile> { dependsOn(tasks.spotlessApply) }

configure<com.diffplug.gradle.spotless.SpotlessExtension> {
  kotlin {
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
    licenseHeader("")
    toggleOffOn()
  }
  kotlinGradle {
    target("*.gradle.kts")
    ktfmt()
    trimTrailingWhitespace()
    endWithNewline()
  }
}

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport, tasks.jacocoTestCoverageVerification)

  testLogging {
    events = setOf(STANDARD_ERROR)
    exceptionFormat = FULL
    showExceptions = true
    showCauses = true
    showStackTraces = true

    afterSuite(
        KotlinClosure2(
            { desc: TestDescriptor, result: TestResult ->
              if (desc.parent == null) {
                println(
                    "Test ${result.resultType} (" +
                        "${result.testCount} tests, " +
                        "${result.successfulTestCount} passed, " +
                        "${result.failedTestCount} failed, " +
                        "${result.skippedTestCount} skipped)")
              }
            },
        ),
    )
  }
}

tasks.withType<JacocoReport> {
  doLast { println("JaCoCo report: file://" + reports.html.entryPoint) }
}

tasks.jacocoTestReport { dependsOn(tasks.test) }

tasks.jacocoTestCoverageVerification {
  dependsOn(tasks.jacocoTestReport)

  violationRules { rule { limit { minimum = "1".toBigDecimal() } } }
}

jmh {
  warmupIterations.set(2)
  iterations.set(2)
  threads.set(1)
  fork.set(2)
}
