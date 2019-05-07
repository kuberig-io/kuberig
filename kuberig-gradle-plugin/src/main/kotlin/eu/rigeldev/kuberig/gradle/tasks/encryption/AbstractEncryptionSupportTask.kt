package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.gradle.config.KubeRigExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input

abstract class AbstractEncryptionSupportTask : DefaultTask() {

    @Input
    lateinit var environment: KubeRigEnvironment

    protected fun environmentEncryptionSupport() : EncryptionSupport {
        return this.project.extensions.getByType(KubeRigExtension::class.java)
            .encryptionSupportFactory
            .forEnvironment(this.project.rootDir, this.environment)
    }

}