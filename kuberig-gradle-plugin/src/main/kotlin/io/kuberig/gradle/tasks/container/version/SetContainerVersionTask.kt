package io.kuberig.gradle.tasks.container.version

import io.kuberig.gradle.tasks.container.version.AbstractContainerVersionTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.options.Option

open class SetContainerVersionTask : AbstractContainerVersionTask() {

    @Input
    var containerVersion: String = ""
        @Option(option = "containerVersion", description = "The container version.")
        set

    override fun startCheck(): Boolean {
        return if (this.containerAlias == "" || this.containerVersion == "") {
            println("--containerAlias and --containerVersion are required!")
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