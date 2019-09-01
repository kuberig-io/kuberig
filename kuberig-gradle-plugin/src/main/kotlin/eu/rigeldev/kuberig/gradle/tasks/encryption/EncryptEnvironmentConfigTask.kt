package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class EncryptEnvironmentConfigTask : AbstractEncryptionSupportTask() {

    @Option(option= "key", description = "Key of the value to encrypt")
    @get:Input
    var key: String = ""

    @TaskAction
    fun encryptEnvironmentConfig() {
        if (key == "") {
            println("--key is required, nothing to encrypt")
        } else {
            val environmentFileSystem = this.environmentFileSystem()
            val updated = environmentFileSystem.encryptConfig(this.key)

            if (!updated) {
                println("No config found for $key in ${environmentFileSystem.environmentConfigsFile.absolutePath}")
            }
        }
    }

}