plugins {
    kotlin("jvm") version "1.9.23"
    kotlin("plugin.serialization") version "1.9.23"
}

group = "gay.extremist"
version = "1.0-SNAPSHOT"

val ktor_version = "2.3.10"
repositories {
    mavenCentral()
}

dependencies {
    implementation("io.ktor:ktor-client-cio-jvm:2.3.10")
    testImplementation(kotlin("test"))
    implementation("io.ktor:ktor-client-core:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")


    // Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.40.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.40.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.40.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.40.1")
    implementation("org.postgresql:postgresql:42.5.0") // Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.4.1")

}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}