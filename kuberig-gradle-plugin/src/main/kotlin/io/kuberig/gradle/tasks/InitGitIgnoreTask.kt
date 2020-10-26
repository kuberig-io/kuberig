package io.kuberig.gradle.tasks

import io.kuberig.gradle.tasks.AbstractKubeRigTask
import org.gradle.api.tasks.TaskAction

open class InitGitIgnoreTask : AbstractKubeRigTask() {

    @TaskAction
    fun init() {
        this.kubeRigExtension().rootFileSystem()
            .gitIgnoresFile.init()
    }
}