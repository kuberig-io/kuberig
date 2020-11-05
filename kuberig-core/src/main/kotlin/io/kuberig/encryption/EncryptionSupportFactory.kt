package io.kuberig.encryption

import io.kuberig.fs.EnvironmentFileSystem
import io.kuberig.fs.RootFileSystem


interface EncryptionSupportFactory {

    fun initForEnvironment(environmentFileSystem: EnvironmentFileSystem)

    fun forEnvironment(rootFileSystem: RootFileSystem, environmentName: String): EncryptionSupport

}