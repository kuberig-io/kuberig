package io.kuberig.gradle.tasks.encryption

import io.kuberig.encryption.EncryptionSupport
import io.kuberig.gradle.tasks.AbstractEnvironmentTask

abstract class AbstractEncryptionSupportTask : AbstractEnvironmentTask() {

    protected fun environmentEncryptionSupport() : EncryptionSupport {
        return encryptionSupportFactory()
            .forEnvironment(this.kubeRigExtension().rootFileSystem(), this.environment.name)
    }

    protected fun encryptionSupportFactory() = this.kubeRigExtension().encryptionSupportFactory()

}