package eu.rigeldev.kuberig.encryption.tink

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import eu.rigeldev.kuberig.encryption.EncryptionSupportFactory
import eu.rigeldev.kuberig.fs.EnvironmentFileSystem
import eu.rigeldev.kuberig.fs.RootFileSystem
import java.io.File

class TinkEncryptionSupportFactory() : EncryptionSupportFactory {

    override fun initForEnvironment(environmentFileSystem: EnvironmentFileSystem) {
        val keySetFile = this.keySetFile(environmentFileSystem)

        if (!keySetFile.exists()) {
            TinkConfig.register()

            // Generate the key material...
            val keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_CTR_HMAC_SHA256)

            // and write it to a file.
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(keySetFile))
        }
    }

    override fun forEnvironment(rootFileSystem: RootFileSystem, environmentName: String): EncryptionSupport {
        val environmentFileSystem = rootFileSystem.environment(environmentName)
        val keySetFile = this.keySetFile(environmentFileSystem)

        return TinkEncryptionSupport(keySetFile)
    }

    private fun keySetFile(environmentFileSystem: EnvironmentFileSystem): File {
        return File(environmentFileSystem.environmentDirectory, "${environmentFileSystem.environmentName}.keyset.json")
    }
}