package io.kuberig.encryption

import java.io.File

/**
 * Contract interface for encryption support.
 */
interface EncryptionSupport {

    fun isValueEncrypted(value: String): Boolean

    fun encryptValue(plainValue: String): String

    fun decryptValue(encryptedValue: String): String

    fun isFileEncrypted(file: File) : Boolean

    fun isFileDecrypted(file: File) : Boolean

    fun encryptFile(plainFile: File): File

    fun decryptFile(encryptedFile: File): File


}