package io.kuberig.fs

import java.io.File

object FileSystemOperations {

    fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            check(directory.mkdirs()) { "Failed to create ${directory.absolutePath}" }
        }
    }

    fun isExistingFile(fileToCheck: File) {
        check(fileToCheck.exists()) { "${fileToCheck.absolutePath} does not exists!" }
        check(fileToCheck.isFile) { "${fileToCheck.absolutePath} is no a file!" }
    }
}