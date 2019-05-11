package eu.rigeldev.kuberig.gradle.tasks.encryption

import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.JsonKeysetWriter
import com.google.crypto.tink.config.TinkConfig
import eu.rigeldev.kuberig.config.KubeRigEnvironment
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import com.google.crypto.tink.KeysetHandle.generateNew
import com.google.crypto.tink.aead.AeadKeyTemplates


open class GenerateEncryptionKeyTask : DefaultTask() {

    @Input
    lateinit var environment : KubeRigEnvironment

    @TaskAction
    fun generateEncryptionKey() {

        TinkConfig.register()

        // Generate the key material...
        val keysetHandle = generateNew(AeadKeyTemplates.AES256_CTR_HMAC_SHA256)

        // and write it to a file.
        val keysetFilename = "environments/${environment.name}/${environment.name}.keyset"
        CleartextKeysetHandle.write(
            keysetHandle, JsonKeysetWriter.withFile(
                this.project.file(keysetFilename)
            )
        )

    }
}