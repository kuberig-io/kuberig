package io.kuberig.fs

import io.kuberig.encryption.EncryptionSupportFactory
import java.io.File

class EnvironmentsFileSystem(private val environmentsDirectory: File,
                             private val rootFileSystem: RootFileSystem,
                             private val encryptionSupportFactory: EncryptionSupportFactory
) {

    val environments = mutableMapOf<String, EnvironmentFileSystem>()

    init {
        if (this.environmentsDirectory.exists()) {
            environmentsDirectory.listFiles()
                ?.filter(File::isDirectory)
                ?.forEach { directory ->  environment(directory.name)}
        }
    }

    fun init() {
        FileSystemOperations.createDirectoryIfNeeded(environmentsDirectory)
    }

    fun environment(environmentName: String): EnvironmentFileSystem {
        if (!this.environments.containsKey(environmentName)) {
            this.environments[environmentName] = EnvironmentFileSystem(
                environmentName,
                File(this.environmentsDirectory, environmentName),
                rootFileSystem,
                encryptionSupportFactory
            )
        }

        return this.environments[environmentName]!!
    }

    fun environmentExists(environmentName: String): Boolean {
        return this.environments.containsKey(environmentName)
    }

}