rootProject.name = "kuberig"

pluginManagement {
    val kotlinVersion: String by settings

    plugins {
        id("org.jetbrains.kotlin.jvm") version(kotlinVersion)
        id("com.jfrog.bintray") version "1.8.5"
    }
}

include("kuberig-core")
include("kuberig-gradle-plugin")
include("kuberig-annotations")
include("kuberig-dsl-support")