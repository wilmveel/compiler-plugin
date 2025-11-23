plugins {
    kotlin("jvm") version "2.2.21"
    id("java-gradle-plugin")
    `maven-publish`
}

group = "community.flock.compiler-plugin"
version = "1.0-SNAPSHOT"

dependencies {
    compileOnly("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
    compileOnly("org.jetbrains.kotlin:kotlin-compiler:2.2.21")
    compileOnly("org.jetbrains.kotlin:kotlin-scripting-compiler-embeddable:2.2.21")

    testImplementation(kotlin("test"))
    // For in-process compilation in integration test (no external test libs)
    testImplementation("org.jetbrains.kotlin:kotlin-compiler-embeddable:2.2.21")
    // Provide @Mapper annotation to the compilation classpath used by the integration test
    testImplementation(project(":compiler-runtime"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Flock Mapper Kotlin Compiler Plugin")
                description.set("Kotlin compiler plugin that generates mapper functions between data classes.")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
