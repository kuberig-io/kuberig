package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.gradle.tasks.AbstractEncryptionSupportTask
import eu.rigeldev.kuberig.support.PropertiesLoaderSupport
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
            val environmentEncryptionSupport = this.environmentEncryptionSupport()

            val environmentConfigFile =
                this.project.file("environments/${environment.name}/${environment.name}-configs.properties")

            val environmentConfig = PropertiesLoaderSupport.loadProperties(environmentConfigFile)

            if (environmentConfig.containsKey(this.key)) {
                val value = environmentConfig.getProperty(this.key)

                val decryptedValue = environmentEncryptionSupport.decryptValue(value)

                println("$key=$decryptedValue")
            } else {
                println("has no value for key $key")
            }
        }
    }

}