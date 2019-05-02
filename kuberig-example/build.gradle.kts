import eu.rigeldev.kuberig.gradle.KubernetesVersion

plugins {
    id("eu.rigeldev.kuberig")
}

repositories {
    jcenter()
}

kuberig {
    kubernetes(KubernetesVersion.V1_12_8)
}