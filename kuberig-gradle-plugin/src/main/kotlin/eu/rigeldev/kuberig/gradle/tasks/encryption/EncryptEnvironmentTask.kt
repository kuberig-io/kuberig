package eu.rigeldev.kuberig.gradle.tasks.encryption

import eu.rigeldev.kuberig.encryption.EncryptionSupport
import java.io.File

open class EncryptEnvironmentTask : AbstractEnvironmentEncryptionTask() {
    override fun processFile(file: File, environmentEncryptionSupport: EncryptionSupport) {
        if (environmentEncryptionSupport.isFileDecrypted(file)) {
            environmentEncryptionSupport.encryptFile(file)

            check(file.delete()) { "Failed to cleanup " + file.absoluteFile + " after encrypting it." }
        }
    }
}