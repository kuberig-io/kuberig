package eu.rigeldev.kuberig.gradle.tasks

import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.gradle.config.KubeRigExtension

abstract class AbstractEncryptionSupportTask : AbstractEnvironmentTask() {

    protected fun environmentEncryptionSupport() : EncryptionSupport {
        return encryptionSupportFactory()
            .forEnvironment(this.project.rootDir, this.environment)
    }

    protected fun encryptionSupportFactory() = this.project.extensions.getByType(KubeRigExtension::class.java)
        .encryptionSupportFactory

}