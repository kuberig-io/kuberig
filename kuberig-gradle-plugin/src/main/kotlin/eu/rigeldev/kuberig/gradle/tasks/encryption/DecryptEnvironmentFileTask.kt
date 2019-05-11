package eu.rigeldev.kuberig.gradle.tasks.encryption

import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class DecryptEnvironmentFileTask: AbstractEncryptionSupportTask() {

    @Option(option= "file", description = "File to encrypt")
    var file : String = ""

    @TaskAction
    fun decryptEnvironmentFile() {
        if (file == "") {
            println("--file is required, nothing to decrypt")
        } else {
            println()
            println("[decrypted] " +
                    this.environmentEncryptionSupport()
                        .decryptFile(this.project.file(this.file))
            )
            println()
        }
    }

}