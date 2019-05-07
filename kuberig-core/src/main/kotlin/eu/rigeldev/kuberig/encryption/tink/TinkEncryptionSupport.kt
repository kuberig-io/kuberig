package eu.rigeldev.kuberig.encryption.tink

import com.google.crypto.tink.config.TinkConfig
import java.io.File
import com.google.crypto.tink.JsonKeysetReader
import com.google.crypto.tink.CleartextKeysetHandle
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.aead.AeadFactory
import eu.rigeldev.kuberig.encryption.EncryptionSupport
import java.util.*

class TinkEncryptionSupport(keysetFile: File) : EncryptionSupport {

    val keysetHandle: KeysetHandle

    init {
        TinkConfig.register()

        keysetHandle = CleartextKeysetHandle.read(
            JsonKeysetReader.withFile(keysetFile)
        )
    }

    override fun encryptValue(plainValue: String): String {
        val aead = AeadFactory.getPrimitive(keysetHandle)

        val ciphertext = aead.encrypt(plainValue.toByteArray(Charsets.UTF_8), "".toByteArray(Charsets.UTF_8))

        return "ENC[" + Base64.getEncoder().encodeToString(ciphertext) + "]"
    }

    override fun decryptValue(encryptedValue: String): String {
        val encryptedBytes = Base64.getDecoder().decode(encryptedValue.substring(4, encryptedValue.length - 1))

        val aead = AeadFactory.getPrimitive(keysetHandle)

        val decryptedBytes = aead.decrypt(encryptedBytes, "".toByteArray(Charsets.UTF_8))

        return decryptedBytes.toString(Charsets.UTF_8)
    }

    override fun isValueEncrypted(value: String): Boolean {
        return value.startsWith("ENC[") && value.endsWith("]")
    }

    override fun encryptFile(plainFile: File): File {
        val aead = AeadFactory.getPrimitive(keysetHandle)

        val ciphertext = aead.encrypt(plainFile.readBytes(), "".toByteArray(Charsets.UTF_8))

        val outputFile = File(plainFile.parentFile, ".encrypted.${plainFile.name.substring(6)}")

        outputFile.writeBytes(ciphertext)

        return outputFile
    }

    override fun isFileEncrypted(file: File): Boolean {
        return file.name.startsWith(".encrypted.")
    }

    override fun isFileDecrypted(file: File): Boolean {
        return file.name.startsWith(".plain.")
    }

    override fun decryptFile(encryptedFile: File): File {
        val encryptedBytes = encryptedFile.readBytes()

        val aead = AeadFactory.getPrimitive(keysetHandle)

        val decryptedBytes = aead.decrypt(encryptedBytes, "".toByteArray(Charsets.UTF_8))

        val outputFile = File(encryptedFile.parentFile, encryptedFile.name.substring(11))

        outputFile.writeBytes(decryptedBytes)

        return outputFile
    }
}