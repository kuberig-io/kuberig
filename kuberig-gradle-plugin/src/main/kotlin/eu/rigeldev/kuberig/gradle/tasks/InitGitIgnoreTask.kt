package eu.rigeldev.kuberig.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

open class InitGitIgnoreTask : DefaultTask() {

    @TaskAction
    fun init() {
        val minimalIgnores = listOf(
            ".plain.*",
            "out",
            "build",
            ".gradle",
            ".idea",
            "*.keyset.json"
        )

        val gitIgnoreFile = this.project.file(".gitignore")

        val gitIgnoreFileLines = if (gitIgnoreFile.exists()) {
            mutableListOf(*gitIgnoreFile.readLines().toTypedArray())
        } else {
            mutableListOf()
        }

        minimalIgnores.forEach {
            if (!gitIgnoreFileLines.contains(it)){
                gitIgnoreFileLines.add(it)
            }
        }

        gitIgnoreFile.writeText(gitIgnoreFileLines.joinToString("\n"))

    }
}