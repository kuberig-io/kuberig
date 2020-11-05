package io.kuberig.gradle.tasks.encryption

import io.kuberig.gradle.tasks.encryption.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class EncryptEnvironmentFileTask: AbstractEncryptionSupportTask() {

    @Option(option= "file", description = "File to encrypt")
    @Input
    var file : String = ""

    @TaskAction
    fun encryptEnvironmentFile() {
        if (file == "") {
            println("--file is required, nothing to encrypt")
        } else {
            val environmentFileSystem = this.environmentFileSystem()

            println()
            println("[encrypted] " +
                    environmentFileSystem
                        .encryptFile(this.project.file(this.file))
            )
            println()
        }
    }
}