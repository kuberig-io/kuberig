package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.encryption.EncryptionSupport
import java.io.File

open class DecryptEnvironmentTask : AbstractEnvironmentEncryptionTask() {

    override fun processFile(
        file: File,
        environmentEncryptionSupport: EncryptionSupport
    ) {

        if (environmentEncryptionSupport.isFileEncrypted(file)) {
            environmentEncryptionSupport.decryptFile(file)
        }
    }
}