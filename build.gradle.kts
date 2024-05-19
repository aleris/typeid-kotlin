import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.*

plugins {
  `java-library`
  alias(libs.plugins.kotlinJvm)
  alias(libs.plugins.spotless)
  jacoco
  alias(libs.plugins.jmh)
  `maven-publish`
  alias(libs.plugins.jreleaser)
  alias(libs.plugins.dokka)
}

group = "earth.adi"

version = "0.0.8"

repositories { mavenCentral() }

dependencies {
  implementation(libs.javaUuidGenerator)
  implementation(platform(libs.jacksonBom))
  implementation(libs.bundles.jackson)

  testImplementation(platform(libs.junitBom))
  testImplementation(libs.bundles.test)
}

kotlin { jvmToolchain(17) }

java {
  withSourcesJar()
  withJavadocJar()
}

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

task<Exec>("updateReadmeVersion") { commandLine("sh", "./scripts/updateReadmeVersion.sh") }

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

  violationRules { rule { limit { minimum = "1.00".toBigDecimal() } } }
}

jmh {
  warmupIterations.set(2)
  iterations.set(2)
  threads.set(1)
  fork.set(2)
}

val mavenArtifactId: String by project
val mavenArtifactDescription: String by project

tasks.jar {
  manifest {
    attributes(
        mapOf(
            "Implementation-Title" to mavenArtifactId, "Implementation-Version" to project.version))
  }
}

val stagingDir: Provider<Directory> = layout.buildDirectory.dir("staging-deploy")

publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = mavenArtifactId
      from(components["java"])
      pom {
        name.set(mavenArtifactId)
        description.set(mavenArtifactDescription)
        url.set("https://github.com/aleris/typeid-kotlin")
        licenses {
          license {
            name.set("The Apache License, Version 2.0")
            url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
          }
        }
        developers {
          developer {
            id.set("aleris")
            name.set("Adrian Tosca")
            email.set("adrian.tosca@gmail.com")
          }
        }
        scm {
          connection.set("scm:git:git@github.com:aleris/typeid-kotlin.git")
          developerConnection.set("scm:git:git@github.com:aleris/typeid-kotlin.git")
          url.set("https://github.com/aleris/typeid-kotlin/")
        }
      }
    }
  }
  repositories { maven { url = stagingDir.get().asFile.toURI() } }
}

tasks {
  register<Jar>("dokkaJar") {
    from(dokkaHtml)
    dependsOn(dokkaHtml)
    archiveClassifier.set("javadoc")
  }
}

tasks.publish { dependsOn(tasks.dokkaHtml) }

jreleaser {
  project {
    description.set(mavenArtifactDescription)
    authors.set(arrayListOf("aleris"))
    license.set("Apache-2.0")
    inceptionYear = "2024"
  }
  release {
    github {
      repoOwner.set("aleris")
      overwrite = true
    }
  }
  signing {
    active.set(org.jreleaser.model.Active.ALWAYS)
    armored = true
  }
  deploy {
    maven {
      mavenCentral {
        register("sonatype") {
          active.set(org.jreleaser.model.Active.ALWAYS)
          url.set("https://central.sonatype.com/api/v1/publisher")
          stagingRepository(stagingDir.get().toString())
        }
      }
    }
  }
}
