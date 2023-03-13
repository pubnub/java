package com.pubnub.apikt.vendor

import com.pubnub.apikt.PNConfiguration.Companion.isValid
import com.pubnub.apikt.PubNub
import com.pubnub.apikt.PubNubException
import java.io.*
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import java.security.SecureRandom
import java.security.spec.AlgorithmParameterSpec
import java.util.*
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object FileEncryptionUtil {
    private const val IV_SIZE_BYTES = 16
    const val ENCODING_UTF_8 = "UTF-8"
    const val CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding"

    /**
     * @see [PubNub.encryptInputStream]
     */
    @Throws(PubNubException::class)
    fun encrypt(inputStream: InputStream, cipherKey: String): InputStream {
        return encryptToBytes(inputStream.readBytes(), cipherKey).inputStream()
    }

    /**
     * @see [PubNub.decryptInputStream]
     */
    @Throws(PubNubException::class)
    fun decrypt(inputStream: InputStream, cipherKey: String): InputStream {
        return try {
            val keyBytes = keyBytes(cipherKey)
            val (ivBytes, dataToDecrypt) = loadIvAndDataFromInputStream(inputStream)
            val decryptionCipher = decryptionCipher(keyBytes, ivBytes)
            val decryptedBytes = decryptionCipher.doFinal(dataToDecrypt)
            ByteArrayInputStream(decryptedBytes)
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is NoSuchPaddingException,
                is InvalidKeyException,
                is IOException,
                is IllegalBlockSizeException,
                is BadPaddingException -> {
                    throw PubNubException(errorMessage = e.message)
                }
                else -> throw e
            }
        }
    }

    internal fun effectiveCipherKey(pubNub: PubNub, cipherKey: String?): String? {
        return cipherKey
            ?: if (pubNub.configuration.cipherKey.isValid()) {
                pubNub.configuration.cipherKey
            } else {
                null
            }
    }

    @Throws(PubNubException::class)
    internal fun encryptToBytes(bytesToEncrypt: ByteArray, cipherKey: String): ByteArray {
        try {
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                val randomIvBytes = randomIv()
                byteArrayOutputStream.write(randomIvBytes)

                val keyBytes = keyBytes(cipherKey)
                val encryptionCipher = encryptionCipher(keyBytes, randomIvBytes)
                byteArrayOutputStream.write(encryptionCipher.doFinal(bytesToEncrypt))
                return byteArrayOutputStream.toByteArray()
            }
        } catch (e: Exception) {
            when (e) {
                is NoSuchAlgorithmException,
                is InvalidAlgorithmParameterException,
                is NoSuchPaddingException,
                is InvalidKeyException,
                is IOException,
                is BadPaddingException,
                is IllegalBlockSizeException -> {
                    throw PubNubException(errorMessage = e.message)
                }
                else -> throw e
            }
        }
    }

    @Throws(IOException::class)
    private fun loadIvAndDataFromInputStream(inputStreamToEncrypt: InputStream): Pair<ByteArray, ByteArray> {
        val ivBytes = ByteArray(IV_SIZE_BYTES)
        inputStreamToEncrypt.read(ivBytes, 0, IV_SIZE_BYTES)
        return ivBytes to inputStreamToEncrypt.readBytes()
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun encryptionCipher(keyBytes: ByteArray, ivBytes: ByteArray): Cipher {
        return cipher(keyBytes, ivBytes, Cipher.ENCRYPT_MODE)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun decryptionCipher(keyBytes: ByteArray, ivBytes: ByteArray): Cipher {
        return cipher(keyBytes, ivBytes, Cipher.DECRYPT_MODE)
    }

    @Throws(
        NoSuchAlgorithmException::class,
        NoSuchPaddingException::class,
        InvalidKeyException::class,
        InvalidAlgorithmParameterException::class
    )
    private fun cipher(keyBytes: ByteArray, ivBytes: ByteArray, mode: Int): Cipher {
        val cipher = Cipher.getInstance(CIPHER_TRANSFORMATION)
        val iv: AlgorithmParameterSpec = IvParameterSpec(ivBytes)
        val key = SecretKeySpec(keyBytes, "AES")
        cipher.init(mode, key, iv)
        return cipher
    }

    @Throws(UnsupportedEncodingException::class, PubNubException::class)
    private fun keyBytes(cipherKey: String): ByteArray {
        return String(
            Crypto.hexEncode(Crypto.sha256(cipherKey.toByteArray(charset(ENCODING_UTF_8)))),
            charset(ENCODING_UTF_8)
        )
            .substring(0, 32)
            .lowercase(Locale.getDefault()).toByteArray(charset(ENCODING_UTF_8))
    }

    @Throws(NoSuchAlgorithmException::class)
    private fun randomIv(): ByteArray {
        val randomIv = ByteArray(IV_SIZE_BYTES)
        SecureRandom.getInstance("SHA1PRNG").nextBytes(randomIv)
        return randomIv
    }
}
