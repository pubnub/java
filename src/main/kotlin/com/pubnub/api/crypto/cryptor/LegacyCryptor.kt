package com.pubnub.api.crypto.cryptor

import com.pubnub.api.crypto.checkMinSize
import com.pubnub.api.crypto.data.EncryptedData
import com.pubnub.api.crypto.data.EncryptedStreamData
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.io.UnsupportedEncodingException
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

private const val STATIC_IV = "0123456789012345"
private const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"
internal val LEGACY_CRYPTOR_ID = ByteArray(4) { 0.toByte() }

private const val IV_SIZE = 16
private const val SIZE_OF_ONE_BLOCK_OF_ENCRYPTED_DATA = 16
private const val RANDOM_IV_STARTING_INDEX = 0
private const val RANDOM_IV_ENDING_INDEX = 15
private const val ENCRYPTED_DATA_STARTING_INDEX = 16 // this is when useRandomIv = true

class LegacyCryptor(val cipherKey: String, val useRandomIv: Boolean = true) : Cryptor {
    private val newKey: SecretKeySpec = createNewKey()

    override fun id(): ByteArray {
        return LEGACY_CRYPTOR_ID // it was agreed that legacy PN Cryptor will have 0 as ID
    }

    override fun encrypt(data: ByteArray): EncryptedData {
        validateData(data)
        return try {
            val ivBytes: ByteArray = getIvBytesForEncryption()
            val cipher = createInitializedCipher(ivBytes, Cipher.ENCRYPT_MODE)
            val encrypted: ByteArray = cipher.doFinal(data)
            if (useRandomIv) {
                EncryptedData(
                    data = ivBytes + encrypted
                )
            } else {
                EncryptedData(
                    data = encrypted
                )
            }
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun decrypt(encryptedData: EncryptedData): ByteArray {
        validateData(encryptedData)
        return try {
            val ivBytes: ByteArray = getIvBytesForDecryption(encryptedData)
            val cipher = createInitializedCipher(ivBytes, Cipher.DECRYPT_MODE)
            val encryptedDataForProcessing = getEncryptedDataForProcessing(encryptedData)
            val decryptedData = cipher.doFinal(encryptedDataForProcessing)
            decryptedData
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun encryptStream(stream: InputStream): EncryptedStreamData {
        val bufferedInputStream = validateStreamAndReturnBuffered(stream)
        try {
            val ivBytes: ByteArray = createRandomIv()
            val cipher = createInitializedCipher(ivBytes, Cipher.ENCRYPT_MODE)
            val cipheredStream = CipherInputStream(bufferedInputStream, cipher)
            return EncryptedStreamData(stream = SequenceInputStream(ivBytes.inputStream(), cipheredStream))
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    override fun decryptStream(encryptedData: EncryptedStreamData): InputStream {
        val bufferedInputStream = validateEncryptedInputStreamAndReturnBuffered(encryptedData.stream)
        try {
            val ivBytes = ByteArray(IV_SIZE)
            val numberOfReadBytes = bufferedInputStream.read(ivBytes)
            if (numberOfReadBytes != IV_SIZE) {
                throw PubNubException(
                    errorMessage = "Could not read IV from encrypted stream",
                    pubnubError = PubNubError.CRYPTO_ERROR
                )
            }
            val cipher = createInitializedCipher(ivBytes, Cipher.DECRYPT_MODE)
            return CipherInputStream(bufferedInputStream, cipher)
        } catch (e: Exception) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    private fun validateEncryptedInputStreamAndReturnBuffered(stream: InputStream): BufferedInputStream {
        val bufferedInputStream = stream.buffered()
        bufferedInputStream.checkMinSize(IV_SIZE + SIZE_OF_ONE_BLOCK_OF_ENCRYPTED_DATA) {
            throw PubNubException(
                errorMessage = "Encryption/Decryption of empty data not allowed.",
                pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
            )
        }
        return bufferedInputStream
    }

    private fun validateStreamAndReturnBuffered(stream: InputStream): BufferedInputStream {
        val bufferedInputStream = stream.buffered()
        bufferedInputStream.checkMinSize(1) {
            throw PubNubException(
                errorMessage = "Encryption/Decryption of empty data not allowed.",
                pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
            )
        }
        return bufferedInputStream
    }

    private fun createNewKey(): SecretKeySpec {
        val keyBytes = String(hexEncode(sha256(cipherKey.toByteArray())), Charsets.UTF_8)
            .substring(0, 32)
            .lowercase(Locale.getDefault()).toByteArray()
        return SecretKeySpec(keyBytes, "AES")
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

    private fun hexEncode(input: ByteArray): ByteArray {
        val result = StringBuilder()
        for (byt in input) {
            result.append(Integer.toString((byt.toInt() and 0xff) + 0x100, 16).substring(1))
        }
        try {
            return result.toString().toByteArray()
        } catch (e: UnsupportedEncodingException) {
            throw PubNubException(errorMessage = e.message, pubnubError = PubNubError.CRYPTO_ERROR)
        }
    }

    private fun validateData(data: ByteArray) {
        if (data.isEmpty()) {
            throw PubNubException(
                errorMessage = "Encryption/Decryption of empty data not allowed.",
                pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
            )
        }
    }

    private fun getIvBytesForEncryption(): ByteArray {
        return if (useRandomIv) {
            createRandomIv()
        } else {
            STATIC_IV.toByteArray()
        }
    }

    private fun createRandomIv(): ByteArray {
        val ivBytes = ByteArray(IV_SIZE)
        SecureRandom().nextBytes(ivBytes)
        return ivBytes
    }

    private fun validateData(encryptedData: EncryptedData) {
        val encryptedDatSize = encryptedData.data.size
        if (useRandomIv) {
            if (encryptedDatSize <= IV_SIZE) {
                throw PubNubException(
                    errorMessage = "Encryption/Decryption of empty data not allowed.",
                    pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
                )
            }
        } else {
            if (encryptedDatSize == 0) {
                throw PubNubException(
                    errorMessage = "Encryption/Decryption of empty data not allowed.",
                    pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
                )
            }
        }
    }

    private fun getIvBytesForDecryption(encryptedData: EncryptedData): ByteArray {
        return if (useRandomIv) {
            encryptedData.data.sliceArray(RANDOM_IV_STARTING_INDEX..RANDOM_IV_ENDING_INDEX)
        } else {
            STATIC_IV.toByteArray()
        }
    }

    private fun createInitializedCipher(iv: ByteArray, mode: Int): Cipher {
        return Cipher.getInstance(CIPHER_TRANSFORMATION).also {
            it.init(mode, newKey, IvParameterSpec(iv))
        }
    }

    private fun getEncryptedDataForProcessing(encryptedData: EncryptedData): ByteArray {
        val encryptedDataForProcessing: ByteArray = if (useRandomIv) {
            encryptedData.data.sliceArray(ENCRYPTED_DATA_STARTING_INDEX until encryptedData.data.size)
        } else {
            // when there is useRandomIv = false then there is no IV in message
            encryptedData.data
        }
        return encryptedDataForProcessing
    }
}
