import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "1.3.30"
}

group = "ninja.tripledip"
version = "0"

repositories {
  mavenCentral()
}

val test by tasks.getting(Test::class) {
  useJUnitPlatform { }
}

dependencies {
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
  implementation("com.squareup.moshi:moshi:1.8.0")
  implementation("com.squareup.moshi:moshi-kotlin:1.8.0")

  testImplementation("io.kotlintest:kotlintest-runner-junit5:3.3.2")
}

tasks.withType<KotlinCompile> {
  kotlinOptions.jvmTarget = "1.8"
}