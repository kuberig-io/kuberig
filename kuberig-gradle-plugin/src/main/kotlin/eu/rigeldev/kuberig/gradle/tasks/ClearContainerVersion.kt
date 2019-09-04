package eu.rigeldev.kuberig.gradle.tasks

open class ClearContainerVersion: AbstractContainerVersionTask() {

    override fun globalAction() {
        rootFileSystem.removeGlobalContainerVersion(this.containerAlias)
    }

    override fun environmentAction() {
        rootFileSystem.removeEnvironmentContainerVersion(this.environmentName, this.containerAlias)
    }
}