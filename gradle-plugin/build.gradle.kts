plugins {
    kotlin("jvm") version "2.2.21"
    id("com.github.gmazzo.buildconfig") version "5.6.5"
    id("java-gradle-plugin")
    id("maven-publish")
}

group = "community.flock.compiler-plugin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(gradleApi())
    compileOnly("org.jetbrains.kotlin:kotlin-gradle-plugin-api:2.2.21")
}

buildConfig {
    packageName(project.group.toString())

    buildConfigField("String", "KOTLIN_PLUGIN_ID", "\"${rootProject.group}\"")

    val pluginProject = project(":compiler-plugin")
    buildConfigField("String", "KOTLIN_PLUGIN_GROUP", "\"${pluginProject.group}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_NAME", "\"${pluginProject.name}\"")
    buildConfigField("String", "KOTLIN_PLUGIN_VERSION", "\"${pluginProject.version}\"")

    val annotationsProject = project(":compiler-runtime")
    buildConfigField(
        type = "String",
        name = "ANNOTATIONS_LIBRARY_COORDINATES",
        expression = "\"${annotationsProject.group}:${annotationsProject.name}:${annotationsProject.version}\""
    )
}

gradlePlugin {
    plugins {
        val mapperCompilerGradlePlugin by creating {
            id = "community.flock.compiler-plugin"
            implementationClass = "community.flock.compilerplugin.gradle.MapperGradlePlugin"
            displayName = "Flock Mapper Kotlin Compiler Plugin"
            description = "Gradle plugin that wires the Mapper Kotlin compiler plugin into Kotlin compile tasks."
        }
    }
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

kotlin {
    jvmToolchain(21)
}
