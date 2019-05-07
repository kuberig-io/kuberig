package eu.rigeldev.kuberig.encryption

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import java.io.File

interface EncryptionSupportFactory {

    fun forEnvironment(projectDirectory: File, environment: KubeRigEnvironment): EncryptionSupport

}