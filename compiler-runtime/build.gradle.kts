plugins {
    kotlin("jvm") version "2.2.21"
    `maven-publish`
}

group = "community.flock.compiler-plugin"
version = "1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
            pom {
                name.set("Flock Mapper Compiler Runtime")
                description.set("Runtime types for the Flock Mapper Kotlin compiler plugin (annotations, etc.)")
            }
        }
    }
    repositories {
        mavenLocal()
    }
}
