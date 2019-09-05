package eu.rigeldev.kuberig.gradle.tasks

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