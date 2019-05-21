package eu.rigeldev.kuberig.gradle.tasks.encryption

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
            println()
            println("[encrypted] " +
                    this.environmentEncryptionSupport()
                        .encryptFile(this.project.file(this.file))
            )
            println()
        }
    }
}