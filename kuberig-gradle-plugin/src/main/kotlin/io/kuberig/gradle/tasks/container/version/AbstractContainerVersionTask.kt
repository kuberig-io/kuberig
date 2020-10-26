package io.kuberig.gradle.tasks.container.version

import io.kuberig.gradle.tasks.AbstractKubeRigTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

abstract class AbstractContainerVersionTask: AbstractKubeRigTask() {

    @Input
    protected var containerAlias: String = ""
        @Option(option = "containerAlias", description = "The container alias to add/update the container version for.")
        set

    @Input
    protected var environmentName: String = ""
        @Option(option = "environment", description = "The name of the environment to add/update the container version for.")
        set

    @Input
    protected val rootFileSystem = this.kubeRigExtension().rootFileSystem()

    @TaskAction
    fun applyContainerVersion() {
        val allInfoAvailable = this.startCheck()
        if (!allInfoAvailable) {
            println("Not all info available")
            return
        }

        val rootFileSystem = this.kubeRigExtension().rootFileSystem()

        if (this.environmentName == "") {
            globalAction()
        } else {
            if (!rootFileSystem.environmentExists(this.environmentName)) {
                println("Environment $environmentName does not exist!")
                return
            } else {
                environmentAction()
            }
        }
    }

    abstract fun startCheck(): Boolean

    abstract fun globalAction()

    abstract fun environmentAction()
}