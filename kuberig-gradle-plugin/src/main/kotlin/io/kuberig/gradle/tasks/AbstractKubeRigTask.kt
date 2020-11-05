package io.kuberig.gradle.tasks

import io.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.DefaultTask

open class AbstractKubeRigTask : DefaultTask() {

    init {
        this.group = "kuberig"
    }

    fun kubeRigExtension() : KubeRigExtension {
        return this.project.extensions.getByType(KubeRigExtension::class.java)
    }

}