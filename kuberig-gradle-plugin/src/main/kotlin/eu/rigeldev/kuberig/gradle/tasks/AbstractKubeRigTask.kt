package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.DefaultTask

open class AbstractKubeRigTask : DefaultTask() {

    fun kubeRigExtension() : KubeRigExtension {
        return this.project.extensions.getByType(KubeRigExtension::class.java)
    }

}