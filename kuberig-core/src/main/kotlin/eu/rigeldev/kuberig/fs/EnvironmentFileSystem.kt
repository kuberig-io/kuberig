package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import eu.rigeldev.kuberig.kubectl.AccessTokenAuthDetail
import eu.rigeldev.kuberig.kubectl.AuthDetails
import eu.rigeldev.kuberig.kubectl.NoAuthDetails
import eu.rigeldev.kuberig.support.PropertiesLoaderSupport
import eu.rigeldev.kuberig.support.PropertiesSupport
import java.io.File
import java.util.*

class EnvironmentFileSystem(
    val environmentName: String,
    val environmentDirectory: File,
    val rootFileSystem: RootFileSystem,
    private val encryptionSupportFactory: EncryptionSupportFactory
) {
    val clusterCaCertPemFile = File(environmentDirectory, "$environmentName-cluster-ca-cert.pem")
    val environmentConfigsFile = File(environmentDirectory, "$environmentName-configs.properties")
    private val encryptedAccessTokenFile = File(environmentDirectory, ".encrypted.$environmentName.access-token")
    private val plainAccessTokenFile = File(environmentDirectory, ".plain.$environmentName.access-token")

    private val containerVersionsFile = ContainerVersionsFile(environmentDirectory)

    fun init(apiServerUrl: String) {

        FileSystemOperations.createDirectoryIfNeeded(this.environmentDirectory)

        this.initEncryption()
        this.initConfigsFile(apiServerUrl)

        this.containerVersionsFile.init()
    }

    fun storeClusterCertificateAuthorityData(certificateAuthorityData: String) {
        clusterCaCertPemFile.writeText(certificateAuthorityData)
    }

    fun storeAccessToken(accessToken: String) {
        val environmentEncryptionSupport = this.encryptionSupport()

        plainAccessTokenFile.writeText(
            String(Base64.getDecoder().decode(accessToken))
        )

        environmentEncryptionSupport.encryptFile(plainAccessTokenFile)

        plainAccessTokenFile.delete()
    }

    fun readAuthDetails() : AuthDetails {
        return if (this.encryptedAccessTokenFile.exists()) {
            val encryptionSupport = this.encryptionSupport()

            val decryptedAccessTokenFile = encryptionSupport.decryptFile(this.encryptedAccessTokenFile)
            try {
                val token = decryptedAccessTokenFile.readText()
                AccessTokenAuthDetail(token)
            } finally {
                decryptedAccessTokenFile.delete()
            }
        } else {
            NoAuthDetails
        }
    }

    fun initEncryption() {
        this.encryptionSupportFactory.initForEnvironment(this)
    }

    private fun initConfigsFile(apiServerUrl: String) {
        val environmentEncryptionSupport = this.encryptionSupport()

        val encryptedApiServerUrl = environmentEncryptionSupport.encryptValue(apiServerUrl)

        val currentLines = if (environmentConfigsFile.exists()) {
            environmentConfigsFile.readLines()
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
                newLines.add(currentLine.trim())
            }
        }

        if (!updated) {
            newLines.add("$apiServerUrlPropertyKey=$encryptedApiServerUrl")
        }

        environmentConfigsFile.writeText(newLines.joinToString("\n"))
    }

    fun decryptConfig(key: String): Boolean {
        val environmentEncryptionSupport = this.encryptionSupport()

        return PropertiesSupport.changeConfig(
            this.environmentConfigsFile,
            key,
            environmentEncryptionSupport::decryptValue
        )
    }

    fun encryptConfig(key: String): Boolean {
        val environmentEncryptionSupport = this.encryptionSupport()

        return PropertiesSupport.changeConfig(
            this.environmentConfigsFile,
            key,
            environmentEncryptionSupport::encryptValue
        )
    }

    fun readConfig(key: String): String? {
        val environmentConfig = this.loadConfigs()

        return if (environmentConfig.containsKey(key)) {
            val value = environmentConfig.getProperty(key)

            encryptionSupport().decryptValue(value)
        } else {
            null
        }
    }

    fun decryptFile(file: File): File {
        return this.encryptionSupport().decryptFile(file)
    }

    fun encryptFile(file: File): File {
        return this.encryptionSupport().encryptFile(file)
    }

    fun encryptionSupport(): EncryptionSupport {
        return this.encryptionSupportFactory
            .forEnvironment(this.rootFileSystem, this.environmentName)
    }

    fun loadConfigs(): Properties {
        return PropertiesLoaderSupport.loadProperties(this.environmentConfigsFile)
    }

    fun generatedYamlDirectory() : File {
        val buildDirectory = File(this.rootFileSystem.repoRootDir, "build")
        val generatedYamlDirectory = File(buildDirectory,"generated-yaml")

        return File(generatedYamlDirectory, environmentName)
    }

    fun clearGeneratedYamlDirectory() {
        val generatedYamlDirectory = this.generatedYamlDirectory()

        if (generatedYamlDirectory.exists()) {
            check(generatedYamlDirectory.deleteRecursively()) { "Failed to clear the output directory ${generatedYamlDirectory.absolutePath}" }
        }
    }

    fun createGeneratedYamlDirectory() {
        val generatedYamlDirectory = this.generatedYamlDirectory()

        FileSystemOperations.createDirectoryIfNeeded(generatedYamlDirectory)
    }

    fun addOrUpdateContainerVersion(containerAlias: String, containerVersion: String){
        return this.containerVersionsFile.addOrUpdateContainerVersion(containerAlias, containerVersion)
    }

    fun readContainerVersion(containerAlias: String): String? {
        return this.containerVersionsFile.readContainerVersion(containerAlias)
    }

    fun removeContainerVersion(containerAlias: String) {
        return this.containerVersionsFile.removeContainerVersion(containerAlias)
    }
}