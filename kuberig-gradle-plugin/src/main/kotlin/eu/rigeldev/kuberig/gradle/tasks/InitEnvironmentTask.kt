package eu.rigeldev.kuberig.gradle.tasks

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class InitEnvironmentTask: DefaultTask() {

    private var environmentName = ""
    private var apiServerUrl = ""

    @Input
    fun getEnvironmentName(): String {
        return this.environmentName
    }

    @Input
    fun getApiServerUrl(): String {
        return this.apiServerUrl
    }

    @Option(option = "environmentName", description = "The name of the environment that you want to create")
    fun setEnvironmentName(environmentName: String) {
        this.environmentName= environmentName
    }

    @Option(option = "apiServerUrl", description = "The URL of the api server of your cluster")
    fun setApiServerUrl(apiServerUrl: String) {
        this.apiServerUrl = apiServerUrl
    }

    @TaskAction
    fun createEnvironment() {

        val environmentsDirectory = this.project.file("environments")
        this.createDirectoryIfNeeded(environmentsDirectory)

        val environmentDirectory = File(environmentsDirectory, this.environmentName)
        this.createDirectoryIfNeeded(environmentDirectory)

        val keysetFile = File(environmentDirectory, "${environmentName}.keyset.json")
        if (!keysetFile.exists()) {
            TinkConfig.register()

            // Generate the key material...
            val keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_CTR_HMAC_SHA256)

            // and write it to a file.
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(keysetFile))
        }

        val kubeRigExtension = this.project.extensions.getByType(KubeRigExtension::class.java)
        val environmentEncryptionSupport = kubeRigExtension
            .encryptionSupportFactory.forEnvironment(this.project.rootDir, KubeRigEnvironment(environmentName))

        val encryptedApiServerUrl = environmentEncryptionSupport.encryptValue(this.apiServerUrl)

        val environmentConfigFile = File(environmentDirectory, "${environmentName}-config.properties")
        val currentLines = if (environmentConfigFile.exists()) {
            environmentConfigFile.readLines()
        } else {
            listOf()
        }

        val newLines = mutableListOf<String>()

        var updated = false
        val apiServerUrlPropertyKey = "api.server.url"
        for (currentLine in currentLines) {
            if (currentLine.trim().toLowerCase().startsWith(apiServerUrlPropertyKey)) {
                newLines.add("$apiServerUrlPropertyKey=$encryptedApiServerUrl")
                updated = true
            } else {
                newLines.add(currentLine)
            }
        }

        if (!updated) {
            newLines.add("$apiServerUrlPropertyKey=$encryptedApiServerUrl")
        }

        environmentConfigFile.writeText(newLines.joinToString("\\n"))
    }

    private fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw IllegalStateException("Failed to create ${directory.absolutePath}")
            }
        }
    }

}