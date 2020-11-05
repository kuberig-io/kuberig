package io.kuberig.gradle.tasks.encryption

import io.kuberig.gradle.tasks.encryption.AbstractEncryptionSupportTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class DecryptEnvironmentConfigTask : AbstractEncryptionSupportTask() {

    @Option(option= "key", description = "Key of the value to decrypt")
    @get:Input
    var key: String = ""

    @TaskAction
    fun decryptEnvironmentConfig() {
        if (key == "") {
            println("--key is required, nothing to decrypt")
        } else {
            val environmentFileSystem = this.environmentFileSystem()
            val updated = environmentFileSystem.decryptConfig(this.key)

            if (!updated) {
                println("No config found for $key in ${environmentFileSystem.environmentConfigsFile.absolutePath}")
            }
        }
    }
}