package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class DecryptEnvironmentFileTask: AbstractEncryptionSupportTask() {

    @Option(option= "file", description = "File to encrypt")
    @Input
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