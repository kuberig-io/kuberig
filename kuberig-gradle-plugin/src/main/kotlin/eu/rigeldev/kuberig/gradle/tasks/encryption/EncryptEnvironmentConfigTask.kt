package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.AbstractEncryptionSupportTask
import eu.rigeldev.kuberig.support.PropertiesSupport
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
            val environmentEncryptionSupport = this.environmentEncryptionSupport()

            val environmentsConfigFile = this.project.file("environments/${environment.name}/${environment.name}-configs.properties")

            val updated = PropertiesSupport.changeConfig(
                environmentsConfigFile,
                this.key,
                environmentEncryptionSupport::encryptValue
            )

            if (!updated) {
                println("No config found for $key in ${environmentsConfigFile.absolutePath}")
            }
        }
    }

}