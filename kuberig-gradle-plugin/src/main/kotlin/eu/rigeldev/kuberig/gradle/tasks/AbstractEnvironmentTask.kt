package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import org.gradle.api.tasks.Input

abstract class AbstractEnvironmentTask: AbstractKubeRigTask() {

    @Input
    lateinit var environment: KubeRigEnvironment

    fun environmentFileSystem(): EnvironmentFileSystem {
        return this.kubeRigExtension.rootFileSystem().environment(this.environment.name)
    }

}