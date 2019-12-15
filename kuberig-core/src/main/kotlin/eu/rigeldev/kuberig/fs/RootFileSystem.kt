package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import java.io.File

class RootFileSystem(
    val repoRootDir: File,
    encryptionSupportFactory: EncryptionSupportFactory
) {

    val environments = EnvironmentsFileSystem(File(repoRootDir, "environments"), this, encryptionSupportFactory)
    val sources = SourcesFileSystem(File(repoRootDir, "src"))
    val gitIgnoresFile = GitIgnoresFile(File(repoRootDir, ".gitignore"))
    val containerVersionsFile = ContainerVersionsFile(repoRootDir)

    init {
        FileSystemOperations.createDirectoryIfNeeded(repoRootDir)

        this.environments.init()
        this.sources.init()

        this.gitIgnoresFile.init()
        this.containerVersionsFile.init()
    }

    fun environment(environmentName: String): EnvironmentFileSystem {
        return this.environments.environment(environmentName)
    }

    // global level

    fun addOrUpdateGlobalContainerVersion(containerAlias: String, containerVersion: String) {
        return this.containerVersionsFile.addOrUpdateContainerVersion(containerAlias, containerVersion)
    }

    fun readGlobalContainerVersion(containerAlias: String): String? {
        return this.containerVersionsFile.readContainerVersion(containerAlias)
    }

    fun removeGlobalContainerVersion(containerAlias: String) {
        return this.containerVersionsFile.removeContainerVersion(containerAlias)
    }

    // environment level

    fun addOrUpdateEnvironmentContainerVersion(
        environmentName: String,
        containerAlias: String,
        containerVersion: String
    ) {
        val environmentFileSystem = this.environment(environmentName)

        environmentFileSystem.addOrUpdateContainerVersion(containerAlias, containerVersion)
    }

    fun readEnvironmentContainerVersion(environmentName: String, containerAlias: String): String? {
        val environmentFileSystem = this.environment(environmentName)

        return environmentFileSystem.readContainerVersion(containerAlias)
    }

    fun removeEnvironmentContainerVersion(environmentName: String, containerAlias: String) {
        val environmentFileSystem = this.environment(environmentName)

        environmentFileSystem.removeContainerVersion(containerAlias)
    }

    // combined

    fun readContainerVersion(environmentName: String, containerAlias: String): String {
        val globalVersion = this.readGlobalContainerVersion(containerAlias)
        val environmentVersion = this.readEnvironmentContainerVersion(environmentName, containerAlias)

        return if (environmentVersion != null) {
            environmentVersion
        } else if (globalVersion != null) {
            globalVersion
        } else {
            throw IllegalStateException("No global/environment specific version available for containerAlias $containerAlias")
        }
    }

    fun environmentExists(environmentName: String): Boolean {
        return this.environments.environmentExists(environmentName)
    }

}