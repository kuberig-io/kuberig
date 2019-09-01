package eu.rigeldev.kuberig.encryption

import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import eu.rigeldev.kuberig.fs.RootFileSystem


interface EncryptionSupportFactory {

    fun initForEnvironment(environmentFileSystem: EnvironmentFileSystem)

    fun forEnvironment(rootFileSystem: RootFileSystem, environmentName: String): EncryptionSupport

}