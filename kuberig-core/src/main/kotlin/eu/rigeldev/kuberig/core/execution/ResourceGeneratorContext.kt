package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.fs.RootFileSystem
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * Helper to provide access to environment specific configuration parameters and files.
 */
object ResourceGeneratorContext {
    private val environmentThreadLocal = ThreadLocal<KubeRigEnvironment>()
    private val environmentDirectoryThreadLocal = ThreadLocal<File>()
    private val environmentConfigsThreadLocal = ThreadLocal<Properties>()
    private val environmentEncryptionSupportThreadLocal = ThreadLocal<EncryptionSupport>()
    private val rootFileSystemThreadLocal = ThreadLocal<RootFileSystem>()

    /**
     * The KubeRigEnvironment that is currently being processed.
     */
    fun environment() : KubeRigEnvironment {
        return environmentThreadLocal.get()
    }

    /**
     * Retrieve a property from the environment configs file (project-root/environments/{environment-name}/{environment-name}-configs.properties).
     *
     * @throws IllegalStateException in case the property is not available.
     */
    fun environmentConfig(configName: String) : String {
        val environmentConfigs = this.environmentConfigsThreadLocal.get()

        val environmentConfigValue = environmentConfigs.getProperty(configName)
            ?: throw IllegalStateException("Config $configName is not available in the ${environment().name} environment configs.")

        return this.decryptEnvironmentConfigValueIfNeeded(environmentConfigValue)
    }

    /**
     * Retrieve a property from the environment configs file (environment/{environment-name}/{environment-name}-configs.properties).
     *
     * Returns the default value in case the property is not available.
     */
    fun environmentConfig(configName: String, defaultValue: String): String {
        val environmentConfigs = this.environmentConfigsThreadLocal.get()

        val environmentConfigValue = environmentConfigs.getProperty(configName, defaultValue)

        return this.decryptEnvironmentConfigValueIfNeeded(environmentConfigValue)
    }

    /**
     * Retrieve the text contents of an environment file. The filePath parameter is used relative to the environment directory {project-root}/environments/{environment-name}.
     *
     * By default this method uses UTF-8 as charset.
     */
    fun environmentFileText(filePath: String, charset: Charset = Charsets.UTF_8) : String {
        val environmentFile = this.requiredEnvironmentFile(filePath)

        return environmentFile.readText(charset)
    }

    /**
     * Retrieve the bytes of an environment file. The filePath parameter is used relative to the environment directory {project-root}/environments/{environment-name}.
     */
    fun environmentFileBytes(filePath: String) : ByteArray {
        val environmentFile = this.requiredEnvironmentFile(filePath)

        return environmentFile.readBytes()
    }

    fun containerVersion(containerAlias: String): String {
        return this.rootFileSystemThreadLocal.get().readContainerVersion(
            this.environment().name,
            containerAlias
        )
    }

    private fun requiredEnvironmentFile(filePath: String) : File {
        val environmentFile = requiredFile(
            this.environmentDirectoryThreadLocal.get(),
            filePath
        ) { candidateFile ->
            "Environment file ${candidateFile.absolutePath} is not available for the ${environment().name} environment."
        }

        val environmentEncryptionSupport = this.environmentEncryptionSupportThreadLocal.get()

        return if (environmentEncryptionSupport.isFileEncrypted(environmentFile)) {
            environmentEncryptionSupport.decryptFile(environmentFile)
        } else {
            environmentFile
        }
    }

    private fun requiredFile(parentDirectory: File, filePath: String, unavailableTextProvider: (File) -> String) : File {
        return requiredFile(File(parentDirectory, filePath), unavailableTextProvider)
    }

    private fun requiredFile(candidateFile: File, unavailableTextProvider: (File) -> String) : File {
        if (!candidateFile.exists()) {
            throw IllegalStateException(unavailableTextProvider(candidateFile))
        } else {
            return candidateFile
        }
    }

    private fun decryptEnvironmentConfigValueIfNeeded(configValue: String): String {
        val environmentEncryptionSupport = this.environmentEncryptionSupportThreadLocal.get()

        return if (environmentEncryptionSupport.isValueEncrypted(configValue)) {
            environmentEncryptionSupport.decryptValue(configValue)
        } else {
            configValue
        }
    }

    fun fill(environment : KubeRigEnvironment,
             environmentDirectory: File,
             environmentConfigs : Properties,
             environmentEncryptionSupport: EncryptionSupport,
             rootFileSystem: RootFileSystem) {
        this.environmentThreadLocal.set(environment)
        this.environmentDirectoryThreadLocal.set(environmentDirectory)
        this.environmentConfigsThreadLocal.set(environmentConfigs)
        this.environmentEncryptionSupportThreadLocal.set(environmentEncryptionSupport)
        this.rootFileSystemThreadLocal.set(rootFileSystem)
    }

    fun clear() {
        environmentThreadLocal.remove()
        environmentDirectoryThreadLocal.remove()
        environmentConfigsThreadLocal.remove()
        environmentEncryptionSupportThreadLocal.remove()
        rootFileSystemThreadLocal.remove()
    }
}