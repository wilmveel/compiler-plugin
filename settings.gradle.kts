plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "compiler-plugin"
include(":compiler-plugin")
include(":gradle-plugin")
include(":compiler-runtime")