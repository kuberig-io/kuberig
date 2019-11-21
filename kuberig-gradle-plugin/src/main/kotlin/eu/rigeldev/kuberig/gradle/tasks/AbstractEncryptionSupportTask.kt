package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.encryption.EncryptionSupport

abstract class AbstractEncryptionSupportTask : AbstractEnvironmentTask() {

    protected fun environmentEncryptionSupport() : EncryptionSupport {
        return encryptionSupportFactory()
            .forEnvironment(this.kubeRigExtension().rootFileSystem(), this.environment.name)
    }

    protected fun encryptionSupportFactory() = this.kubeRigExtension().encryptionSupportFactory()

}