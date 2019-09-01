package eu.rigeldev.kuberig.fs

import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import java.io.File

class RootFileSystem(val repoRootDir: File,
                     encryptionSupportFactory: EncryptionSupportFactory) {

    val environments = EnvironmentsFileSystem(File(repoRootDir, "environments"), this, encryptionSupportFactory)
    val sources = SourcesFileSystem(File(repoRootDir, "src"))
    val gitIgnoresFile = GitIgnoresFile(File(repoRootDir, ".gitignores"))

    init {
        FileSystemOperations.createDirectoryIfNeeded(repoRootDir)

        this.environments.init()
        this.sources.init()

        this.gitIgnoresFile.init()
    }

    fun environment(environmentName: String): EnvironmentFileSystem {
        return this.environments.environment(environmentName)
    }

}