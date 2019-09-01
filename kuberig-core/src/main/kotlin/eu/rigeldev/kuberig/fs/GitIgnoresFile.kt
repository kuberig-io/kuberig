package eu.rigeldev.kuberig.fs

import java.io.File

class GitIgnoresFile(private val gitIgnoreFile: File) {

    private val minimalIgnores = listOf(
        ".plain.*",
        "out",
        "build",
        ".gradle",
        ".idea",
        "*.keyset.json"
    )

    fun init() {
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