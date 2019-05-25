package eu.rigeldev.kuberig.gradle.tasks.encryption

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadKeyTemplates
import com.google.crypto.tink.config.TinkConfig
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File


open class GenerateEncryptionKeyTask : DefaultTask() {

    @Input
    lateinit var environment : KubeRigEnvironment

    @TaskAction
    fun generateEncryptionKey() {

        val environmentsDirectory = this.project.file("environments")
        this.createDirectoryIfNeeded(environmentsDirectory)

        val environmentDirectory = File(environmentsDirectory, this.environment.name)
        this.createDirectoryIfNeeded(environmentDirectory)

        val keysetFile = File(environmentDirectory, "${this.environment.name}.keyset.json")
        if (!keysetFile.exists()) {
            TinkConfig.register()

            // Generate the key material...
            val keysetHandle = KeysetHandle.generateNew(AeadKeyTemplates.AES256_CTR_HMAC_SHA256)

            // and write it to a file.
            CleartextKeysetHandle.write(keysetHandle, JsonKeysetWriter.withFile(keysetFile))
        }

    }

    private fun createDirectoryIfNeeded(directory: File) {
        if (!directory.exists()) {
            if (!directory.mkdir()) {
                throw IllegalStateException("Failed to create ${directory.absolutePath}")
            }
        }
    }
}