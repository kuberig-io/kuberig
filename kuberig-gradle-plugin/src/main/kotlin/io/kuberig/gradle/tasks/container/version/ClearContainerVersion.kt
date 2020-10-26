package io.kuberig.gradle.tasks.container.version

import io.kuberig.gradle.tasks.container.version.AbstractContainerVersionTask

open class ClearContainerVersion: AbstractContainerVersionTask() {

    override fun startCheck(): Boolean {
        return if (this.containerAlias == "") {
            println("--alias is required!")
            false
        } else {
            true
        }
    }

    override fun globalAction() {
        rootFileSystem.removeGlobalContainerVersion(this.containerAlias)
    }

    override fun environmentAction() {
        rootFileSystem.removeEnvironmentContainerVersion(this.environmentName, this.containerAlias)
    }
}