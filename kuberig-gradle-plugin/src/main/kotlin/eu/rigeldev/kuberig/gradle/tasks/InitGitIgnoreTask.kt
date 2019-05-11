package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class InitGitIgnoreTask : DefaultTask() {

    @TaskAction
    fun init() {
        val plainFileIgnore = ".plain.*"

        val gitIgnoreFile = this.project.file(".gitignore")

        val gitIgnoreFileLines = if (gitIgnoreFile.exists()) {
            mutableListOf(*gitIgnoreFile.readLines().toTypedArray())
        } else {
            mutableListOf()
        }

        if (!gitIgnoreFileLines.contains(plainFileIgnore)){
            gitIgnoreFileLines.add(plainFileIgnore)
        }

        gitIgnoreFile.writeText(gitIgnoreFileLines.joinToString("\n"))

    }
}