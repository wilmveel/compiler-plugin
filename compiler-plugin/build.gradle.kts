plugins {
    kotlin("jvm") version "2.2.21"
}

group = "community.flock.compiler-plugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.2.21")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.2.21")

    testImplementation(kotlin("test"))
    // For in-process compilation in integration test (no external test libs)
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}
