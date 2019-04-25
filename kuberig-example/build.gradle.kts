buildscript {
    dependencies {
        classpath("eu.rigeldev.kuberig:kuberig-gradle-plugin:0.0.9")
    }
}

plugins {
    id("eu.rigeldev.kuberig")
}

repositories {
    jcenter()
}

dependencies {
    implementation("eu.rigeldev.kuberig.dsl.kubernetes:kuberig-dsl-kubernetes-v1.15.0-alpha.1:0.0.9")
}