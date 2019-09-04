package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.tasks.options.Option

open class SetContainerVersionTask : AbstractContainerVersionTask() {

    @Option(option = "version", description = "The container version.")
    protected var containerVersion: String = ""

    override fun globalAction() {
        rootFileSystem.addOrUpdateGlobalContainerVersion(this.containerAlias, this.containerVersion)
    }

    override fun environmentAction() {
        rootFileSystem.addOrUpdateEnvironmentContainerVersion(this.environmentName, this.containerAlias, this.containerVersion)
    }
}