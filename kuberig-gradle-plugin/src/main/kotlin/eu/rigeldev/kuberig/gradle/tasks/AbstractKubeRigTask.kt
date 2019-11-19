package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

open class AbstractKubeRigTask : DefaultTask() {
    @Input
    val kubeRigExtension : KubeRigExtension = this.project.extensions.getByType(KubeRigExtension::class.java)

}