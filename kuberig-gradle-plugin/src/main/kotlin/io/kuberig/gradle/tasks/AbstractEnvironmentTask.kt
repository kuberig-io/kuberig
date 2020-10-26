package io.kuberig.gradle.tasks

import io.kuberig.config.KubeRigEnvironment
import io.kuberig.fs.EnvironmentFileSystem
import org.gradle.api.tasks.Input

abstract class AbstractEnvironmentTask: AbstractKubeRigTask() {

    @Input
    lateinit var environment: KubeRigEnvironment

    fun environmentFileSystem(): EnvironmentFileSystem {
        return this.kubeRigExtension().rootFileSystem().environment(this.environment.name)
    }

}