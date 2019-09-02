package eu.rigeldev.kuberig.fs

import java.io.File

object FileSystemOperations {

    fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            check(directory.mkdirs()) { "Failed to create ${directory.absolutePath}" }
        }
    }

}