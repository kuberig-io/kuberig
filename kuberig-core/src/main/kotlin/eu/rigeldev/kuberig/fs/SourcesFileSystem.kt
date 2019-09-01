package eu.rigeldev.kuberig.fs

import java.io.File

class SourcesFileSystem(private val srcDirectory: File) {

    private val mainSourcesDirectory = File(srcDirectory, "main")
    private val mainKotlinSourcesDirectory = File(mainSourcesDirectory, "kotlin")

    fun init() {
        FileSystemOperations.createDirectoryIfNeeded(srcDirectory)
        FileSystemOperations.createDirectoryIfNeeded(mainSourcesDirectory)
        FileSystemOperations.createDirectoryIfNeeded(mainKotlinSourcesDirectory)
    }
}
