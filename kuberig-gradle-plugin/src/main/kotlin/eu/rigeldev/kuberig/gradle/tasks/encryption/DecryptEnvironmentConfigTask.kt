package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.support.PropertiesSupport
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

open class DecryptEnvironmentConfigTask : AbstractEncryptionSupportTask() {

    @Option(option= "key", description = "Key of the value to decrypt")
    var key: String = ""

    @TaskAction
    fun decryptEnvironmentConfig() {
        if (key == "") {
            println("--key is required, nothing to decrypt")
        } else {
            val environmentEncryptionSupport = this.environmentEncryptionSupport()

            val environmentsConfigFile = this.project.file("environments/${environment.name}/${environment.name}-config.properties")

            val updated = PropertiesSupport.changeConfig(
                environmentsConfigFile,
                this.key,
                environmentEncryptionSupport::decryptValue
            )

            if (!updated) {
                println("No config found for $key in ${environmentsConfigFile.absolutePath}")
            }
        }
    }
}