package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.encryption.AbstractEncryptionSupportTask
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class DecryptEnvironmentValueTask : AbstractEncryptionSupportTask() {

    @Option(option= "value", description = "Value to decrypt")
    var value : String = ""

    @TaskAction
    fun decryptEnvironmentValue() {
        if (value == "") {
            println("--value is required, nothing to decrypt")
        } else {
            println()
            println("[decrypted] "+ this.environmentEncryptionSupport().decryptValue(this.value))
            println()
        }
    }
}