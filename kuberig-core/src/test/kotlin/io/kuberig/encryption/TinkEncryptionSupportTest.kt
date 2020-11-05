package io.kuberig.encryption

import io.kuberig.encryption.tink.TinkEncryptionSupport
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class TinkEncryptionSupportTest {

    private val keysetFile = File("src/test/resources/test.keyset")
    private val tinkEncryptionSupport = TinkEncryptionSupport(keysetFile)

    @Test
    fun encryptDecryptValue() {

        val originalPlainValue = "something-in-need-of-encryption"

        val encryptedValue = this.tinkEncryptionSupport.encryptValue(originalPlainValue)

        assertNotEquals(originalPlainValue, encryptedValue)

        val decryptedPlainValue = this.tinkEncryptionSupport.decryptValue(encryptedValue)

        assertEquals(originalPlainValue, decryptedPlainValue)
    }

    @Test
    fun encryptDecryptFile() {

        val keysetCopy = File("build/copy.keyset")
        keysetCopy.writeBytes(this.keysetFile.readBytes())

        assertFalse(this.tinkEncryptionSupport.isFileEncrypted(keysetCopy))

        val encryptedFile = this.tinkEncryptionSupport.encryptFile(keysetCopy)

        assertTrue(this.tinkEncryptionSupport.isFileEncrypted(encryptedFile))

        val decryptedFile = this.tinkEncryptionSupport.decryptFile(encryptedFile)

        assertArrayEquals(this.keysetFile.readBytes(), decryptedFile.readBytes())
    }
}