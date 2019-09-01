package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class ShowEnvironmentConfigTask: AbstractEncryptionSupportTask() {

    @Option(option= "key", description = "Key of the value to decrypt")
    @get:Input
    var key: String = ""

    @TaskAction
    fun showEnvironmentConfig() {
        if (key == "") {
            println("--key is required, nothing to decrypt")
        } else {
            val decryptedValue = this.environmentFileSystem().readConfig(this.key)

            if (decryptedValue == null) {
                println("has no value for key $key")
            } else {
                println("$key=$decryptedValue")
            }
        }
    }

}