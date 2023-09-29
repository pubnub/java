package com.pubnub.api.crypto.cryptor

import com.pubnub.api.crypto.data.EncryptedData
import com.pubnub.api.crypto.data.EncryptedStreamData
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import java.io.InputStream
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
private const val RANDOM_IV_SIZE = 16

class AesCbcCryptor(val cipherKey: String) : Cryptor {
    private val newKey: SecretKeySpec = createNewKey()

    override fun id(): ByteArray {
        return byteArrayOf('A'.code.toByte(), 'C'.code.toByte(), 'R'.code.toByte(), 'H'.code.toByte())
    }

    override fun encrypt(data: ByteArray): EncryptedData {
        return try {
            val ivBytes: ByteArray = createRandomIv()
            val cipher = createInitializedCipher(ivBytes, Cipher.ENCRYPT_MODE)
            val encryptedData: ByteArray = cipher.doFinal(data)
            EncryptedData(metadata = ivBytes, data = encryptedData)
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun decrypt(encryptedData: EncryptedData): ByteArray {
        return try {
            val ivBytes: ByteArray = encryptedData.metadata?.takeIf { it.size == RANDOM_IV_SIZE } ?: throw PubNubException("Invalid random IV")
            val cipher = createInitializedCipher(ivBytes, Cipher.DECRYPT_MODE)
            val decryptedData = cipher.doFinal(encryptedData.data)
            decryptedData
        } catch (e: Exception) {
            throw PubNubException(errorMessage = "Decryption error", pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun encryptStream(stream: InputStream): EncryptedStreamData {
        try {
            val ivBytes: ByteArray = createRandomIv()
            val cipher = createInitializedCipher(ivBytes, Cipher.ENCRYPT_MODE)
            val cipheredStream = CipherInputStream(stream, cipher)

            return EncryptedStreamData(
                metadata = ivBytes,
                stream = cipheredStream
            )
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun decryptStream(encryptedData: EncryptedStreamData): InputStream {
        try {
            val ivBytes: ByteArray = encryptedData.metadata?.takeIf { it.size == RANDOM_IV_SIZE } ?: throw PubNubException("Invalid random IV")
            val cipher = createInitializedCipher(ivBytes, Cipher.DECRYPT_MODE)
            return CipherInputStream(encryptedData.stream, cipher)
        } catch (e: Exception) {
            throw PubNubException(errorMessage = "Decryption error", pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    private fun createInitializedCipher(iv: ByteArray, mode: Int): Cipher {
        return Cipher.getInstance(CIPHER_TRANSFORMATION).also {
            it.init(mode, newKey, IvParameterSpec(iv))
        }
    }

    private fun createNewKey(): SecretKeySpec {
        val keyBytes = sha256(cipherKey.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(keyBytes, "AES")
    }

    private fun createRandomIv(): ByteArray {
        val ivBytes = ByteArray(RANDOM_IV_SIZE)
        SecureRandom().nextBytes(ivBytes)
        return ivBytes
    }

    private fun sha256(input: ByteArray): ByteArray {
        val digest: MessageDigest
        return try {
            digest = MessageDigest.getInstance("SHA-256")
            digest.digest(input)
        } catch (e: java.lang.Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }
}
