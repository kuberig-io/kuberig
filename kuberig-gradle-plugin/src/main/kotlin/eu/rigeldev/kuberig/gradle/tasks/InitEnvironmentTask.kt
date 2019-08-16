package eu.rigeldev.kuberig.gradle.tasks

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import eu.rigeldev.kuberig.init.ServiceAccountCreator
import eu.rigeldev.kuberig.kubectl.ErrorContextResult
import eu.rigeldev.kuberig.kubectl.KubectlConfigReader
import eu.rigeldev.kuberig.kubectl.OkContextResult
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option
import java.io.File

abstract class InitEnvironmentTask: DefaultTask() {

    private var environmentName = ""
    private var apiServerUrl = ""
    private var currentKubectlContext = false

    @Input
    fun getEnvironmentName(): String {
        return this.environmentName
    }

    @Input
    fun getApiServerUrl(): String {
        return this.apiServerUrl
    }

    @Option(option = "name", description = "The name of the environment that you want to create")
    fun setEnvironmentName(environmentName: String) {
        this.environmentName= environmentName
    }

    @Option(option = "apiServerUrl", description = "The URL of the api server of your cluster")
    fun setApiServerUrl(apiServerUrl: String) {
        this.apiServerUrl = apiServerUrl
    }

    @Option(option = "currentKubectlContext", description = "From the current kubectl context")
    fun setCurrentKubectlContext(currentKubectlContext: Boolean) {
        this.currentKubectlContext = currentKubectlContext
    }

    @TaskAction
    fun createEnvironment() {

        if (this.environmentName == "") {
            println("--name is required")
            return
        }

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

        if (currentKubectlContext) {

            val kubectlConfigReader = KubectlConfigReader()
            val contextResult = kubectlConfigReader.readKubectlConfig()


            when (contextResult) {
                is OkContextResult -> {
                    val serviceAccountCreator = ServiceAccountCreator(kubeRigExtension.flags)

                    serviceAccountCreator.createDefaultServiceAccount(this.environmentName, contextResult, environmentEncryptionSupport, environmentDirectory)

                    this.apiServerUrl = contextResult.clusterDetail.server
                }
                is ErrorContextResult -> {
                    println("Failed to read current kubectl context:[error]${contextResult.error}")
                }
            }

        }

        val encryptedApiServerUrl = environmentEncryptionSupport.encryptValue(this.apiServerUrl)

        val environmentConfigFile = File(environmentDirectory, "${environmentName}-configs.properties")
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

        val srcDirectory = this.project.file("src")
        this.createDirectoryIfNeeded(srcDirectory)

        val mainSrcDirectory = File(srcDirectory, "main")
        this.createDirectoryIfNeeded(mainSrcDirectory)

        val mainKotlinSrcDirectory = File(mainSrcDirectory, "kotlin")
        this.createDirectoryIfNeeded(mainKotlinSrcDirectory)

    }

    private fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw IllegalStateException("Failed to create ${directory.absolutePath}")
            }
        }
    }

    private fun readKubectlConfig() {



    }

}