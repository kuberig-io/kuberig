package io.kuberig.gradle.tasks.encryption

import io.kuberig.encryption.EncryptionSupport
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class AbstractEnvironmentEncryptionTask : AbstractEncryptionSupportTask() {

    @TaskAction
    fun processEnvironment() {
        val environmentDirectory = this.environmentFileSystem().environmentDirectory

        this.processFileOrDirectory(environmentDirectory, this.environmentEncryptionSupport())
    }

    private fun processFileOrDirectory(fileOrDirectory: File, environmentEncryptionSupport: EncryptionSupport) {

        if (fileOrDirectory.isDirectory) {
            fileOrDirectory.listFiles()?.forEach { this.processFileOrDirectory(it, environmentEncryptionSupport) }
        } else {
            this.processFile(fileOrDirectory, environmentEncryptionSupport)
        }

    }

    abstract fun processFile(
        file: File,
        environmentEncryptionSupport: EncryptionSupport
    )

}