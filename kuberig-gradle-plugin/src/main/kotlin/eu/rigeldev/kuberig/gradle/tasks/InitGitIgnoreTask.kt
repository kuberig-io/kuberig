package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.tasks.TaskAction

open class InitGitIgnoreTask : AbstractKubeRigTask() {

    @TaskAction
    fun init() {
        this.kubeRigExtension().rootFileSystem()
            .gitIgnoresFile.init()
    }
}