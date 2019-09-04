package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.tasks.options.Option

abstract class AbstractContainerVersionTask: AbstractKubeRigTask() {

    @Option(option = "alias", description = "The container alias to add/update the container version for.")
    protected var containerAlias: String = ""

    @Option(option = "environment", description = "The name of the environment to add/update the container version for.")
    protected var environmentName: String = ""

    protected val rootFileSystem = this.kubeRigExtension.rootFileSystem()

    abstract fun globalAction()

    abstract fun environmentAction()
}