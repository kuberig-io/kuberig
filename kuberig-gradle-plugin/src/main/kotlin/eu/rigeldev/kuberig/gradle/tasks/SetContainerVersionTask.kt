package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.tasks.options.Option

open class SetContainerVersionTask : AbstractContainerVersionTask() {

    var containerVersion: String = ""
        @Option(option = "containerVersion", description = "The container version.")
        set

    override fun startCheck(): Boolean {
        return if (this.containerAlias == "" || this.containerVersion == "") {
            println("--alias and --version are required!")
            false
        } else {
            true
        }
    }

    override fun globalAction() {
        rootFileSystem.addOrUpdateGlobalContainerVersion(this.containerAlias, this.containerVersion)
    }

    override fun environmentAction() {
        rootFileSystem.addOrUpdateEnvironmentContainerVersion(this.environmentName, this.containerAlias, this.containerVersion)
    }
}