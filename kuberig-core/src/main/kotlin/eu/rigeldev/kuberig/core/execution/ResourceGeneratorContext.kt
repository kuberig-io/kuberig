package eu.rigeldev.kuberig.core.execution

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import java.io.File
import java.nio.charset.Charset
import java.util.*

/**
 * Helper to provide access to global and environment specific configuration parameters and files.
 */
object ResourceGeneratorContext {
    private val environment = ThreadLocal<KubeRigEnvironment>()
    private val projectDirectory = ThreadLocal<File>()
    private val environmentDirectory = ThreadLocal<File>()
    private val environmentConfigs = ThreadLocal<Properties>()
    private val globalConfigs = ThreadLocal<Properties>()

    /**
     * The KubeRigEnvironment that is currently being processed.
     */
    fun environment() : KubeRigEnvironment {
        return environment.get()
    }

    /**
     * Retrieve a property from the environment configs file (project-root/environments/{environment-name}/{environment-name}-configs.properties).
     *
     * @throws IllegalStateException in case the property is not available.
     */
    fun environmentConfig(configName: String) : String {
        return this.environmentConfigs.get().getProperty(configName) ?: throw IllegalStateException("Config $configName is not available in the ${environment.get().name} environment configs.")
    }

    /**
     * Retrieve a property from the environment configs file (environment/{environment-name}/{environment-name}-configs.properties).
     *
     * Returns the default value in case the property is not available.
     */
    fun environmentConfig(configName: String, defaultValue: String): String {
        return this.environmentConfigs.get().getProperty(configName, defaultValue)
    }

    /**
     * Retrieve the text contents of an environment file. The filePath parameter is used relative to the environment directory {project-root}/environments/{environment-name}.
     *
     * By default this method uses UTF-8 as charset.
     */
    fun environmentFileText(filePath: String, charset: Charset = Charsets.UTF_8) : String {
        return this.requiredEnvironmentFile(filePath).readText(charset)
    }

    /**
     * Retrieve the bytes of an environment file. The filePath parameter is used relative to the environment directory {project-root}/environments/{environment-name}.
     */
    fun environmentFileBytes(filePath: String) : ByteArray {
        return this.requiredEnvironmentFile(filePath).readBytes()
    }

    /**
     * Retrieve a property from the global configs file (project-root/global-configs.properties).
     *
     * @throws IllegalStateException in case the property is not available.
     */
    fun globalConfig(configName: String): String {
        return this.globalConfigs.get().getProperty(configName) ?: throw IllegalStateException("Config $configName is not available in the global configs.")
    }

    /**
     * Retrieves a property from the global configs file (project-root/global-configs.properties).
     *
     * Returns the default value in case the property is not available.
     */
    fun globalConfig(configName: String, defaultValue: String): String {
        return this.environmentConfigs.get().getProperty(configName, defaultValue)
    }

    /**
     * Retrieve the text contents of a global file. The filePath parameter is used relative to the project root directory.
     *
     * By default this method uses UTF-8 as charset.
     */
    fun globalFileText(filePath: String, charset: Charset = Charsets.UTF_8) : String {
        return this.requiredGlobalFile(filePath).readText(charset)
    }

    /**
     * Retrieve the bytes of a global file. The filePath parameter is used relative to the project root directory.
     */
    fun globalFileBytes(filePath: String) : ByteArray {
        return this.requiredGlobalFile(filePath).readBytes()
    }

    private fun requiredEnvironmentFile(filePath: String) : File {
        return requiredFile(
            this.environmentDirectory.get(),
            filePath
        ) { candidateFile ->
            "Environment file ${candidateFile.absolutePath} is not available for the ${environment.get().name} environment."
        }
    }

    private fun requiredGlobalFile(filePath: String) : File {
        return requiredFile(
            this.projectDirectory.get(),
            filePath
        ) { candidateFile -> "Global file ${candidateFile.absolutePath} is not available." }
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

    fun fill(environment : KubeRigEnvironment,
             projectDirectory: File,
             environmentDirectory: File,
             environmentConfigs : Properties,
             globalConfigs: Properties) {
        this.environment.set(environment)
        this.projectDirectory.set(projectDirectory)
        this.environmentDirectory.set(environmentDirectory)
        this.environmentConfigs.set(environmentConfigs)
        this.globalConfigs.set(globalConfigs)
    }

    fun clear() {
        environment.remove()
        projectDirectory.remove()
        environmentDirectory.remove()
        environmentConfigs.remove()
        globalConfigs.remove()
    }
}