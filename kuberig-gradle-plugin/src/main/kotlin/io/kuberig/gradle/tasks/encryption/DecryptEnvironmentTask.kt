package io.kuberig.gradle.tasks.encryption

import io.kuberig.encryption.EncryptionSupport
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