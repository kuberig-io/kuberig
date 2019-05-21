package eu.rigeldev.kuberig.gradle.tasks.encryption

import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class EncryptEnvironmentValueTask : AbstractEncryptionSupportTask() {

    @Option(option= "value", description = "Value to encrypt")
    @Input
    var value : String = ""

    @TaskAction
    fun encryptEnvironmentValue() {
        if (value == "") {
            println("--value is required, nothing to encrypt")
        } else {
            println()
            println("[encrypted] " + this.environmentEncryptionSupport().encryptValue(this.value))
            println()
        }
    }

}