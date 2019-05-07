package eu.rigeldev.kuberig.encryption.tink

import eu.rigeldev.kuberig.config.KubeRigEnvironment
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import java.io.File

class TinkEncryptionSupportFactory : EncryptionSupportFactory {


    override fun forEnvironment(projectDirectory: File, environment: KubeRigEnvironment): EncryptionSupport {
        val keysetFile = File(projectDirectory, "environments/${environment.name}/${environment.name}.keyset")
        return TinkEncryptionSupport(keysetFile)
    }
}