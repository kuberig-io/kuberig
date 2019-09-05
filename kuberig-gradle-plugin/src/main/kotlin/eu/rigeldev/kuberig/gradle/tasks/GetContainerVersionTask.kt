package eu.rigeldev.kuberig.gradle.tasks

open class GetContainerVersionTask : AbstractContainerVersionTask() {

    override fun startCheck(): Boolean {
        return if (this.containerAlias == "") {
            println("--alias is required!")
            false
        } else {
            true
        }
    }

    override fun globalAction() {
        val containerVersion = this.rootFileSystem.readGlobalContainerVersion(containerAlias)

        if (containerVersion == null) {
            println("No global version available for $containerAlias")
        } else {
            println("The global version for $containerAlias is $containerVersion")
        }
    }

    override fun environmentAction() {
        val containerVersion = this.rootFileSystem.readEnvironmentContainerVersion(environmentName, containerAlias)

        if (containerVersion == null) {
            println("The $environmentName environment does not specify a version for $containerAlias.")
        } else {
            println("The $environmentName environment version for $containerAlias is $containerVersion")
        }
    }
}