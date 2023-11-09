package com.pubnub.api.crypto

import com.pubnub.api.crypto.cryptor.AesCbcCryptor
import com.pubnub.api.crypto.cryptor.Cryptor
import com.pubnub.api.crypto.cryptor.HeaderParser
import com.pubnub.api.crypto.cryptor.LEGACY_CRYPTOR_ID
import com.pubnub.api.crypto.cryptor.LegacyCryptor
import com.pubnub.api.crypto.cryptor.ParseResult
import com.pubnub.api.crypto.data.EncryptedData
import com.pubnub.api.crypto.data.EncryptedStreamData
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import com.pubnub.api.vendor.Base64
import java.io.BufferedInputStream
import java.io.InputStream
import java.io.SequenceInputStream
import java.lang.IllegalArgumentException
import java.lang.Integer.min

class CryptoModule internal constructor(
    internal val primaryCryptor: Cryptor,
    internal val cryptorsForDecryptionOnly: List<Cryptor> = listOf(),
    internal val headerParser: HeaderParser = HeaderParser()
) {

    companion object {
        @JvmStatic
        fun createLegacyCryptoModule(cipherKey: String, randomIv: Boolean = true): CryptoModule {
            return CryptoModule(
                primaryCryptor = LegacyCryptor(cipherKey, randomIv),
                cryptorsForDecryptionOnly = listOf(LegacyCryptor(cipherKey, randomIv), AesCbcCryptor(cipherKey))
            )
        }

        @JvmStatic
        fun createAesCbcCryptoModule(cipherKey: String, randomIv: Boolean = true): CryptoModule {
            return CryptoModule(
                primaryCryptor = AesCbcCryptor(cipherKey),
                cryptorsForDecryptionOnly = listOf(AesCbcCryptor(cipherKey), LegacyCryptor(cipherKey, randomIv))
            )
        }

        @JvmStatic
        fun createNewCryptoModule(
            defaultCryptor: Cryptor,
            cryptorsForDecryptionOnly: List<Cryptor> = listOf()
        ): CryptoModule {
            return CryptoModule(
                primaryCryptor = defaultCryptor,
                cryptorsForDecryptionOnly = listOf(defaultCryptor) + cryptorsForDecryptionOnly
            )
        }
    }

    fun encrypt(data: ByteArray): ByteArray {
        validateData(data)
        val (metadata, encryptedData) = primaryCryptor.encrypt(data)

        return if (primaryCryptor.id().contentEquals(LEGACY_CRYPTOR_ID)) {
            encryptedData
        } else {
            val cryptorHeader = headerParser.createCryptorHeader(primaryCryptor.id(), metadata)
            cryptorHeader + encryptedData
        }
    }

    fun decrypt(encryptedData: ByteArray): ByteArray {
        validateData(encryptedData)
        val parsedData: ParseResult<out ByteArray> = headerParser.parseDataWithHeader(encryptedData)
        val decryptedData: ByteArray = when (parsedData) {
            is ParseResult.NoHeader -> {
                getDecryptedDataForLegacyCryptor(encryptedData)
            }
            is ParseResult.Success -> {
                getDecryptedDataForCryptorWithHeader(parsedData)
            }
        }
        return decryptedData
    }

    fun encryptStream(stream: InputStream): InputStream {
        val bufferedInputStream = validateStreamAndReturnBuffered(stream)
        val (metadata, encryptedData) = primaryCryptor.encryptStream(bufferedInputStream)
        return if (primaryCryptor.id().contentEquals(LEGACY_CRYPTOR_ID)) {
            encryptedData
        } else {
            val cryptorHeader: ByteArray = headerParser.createCryptorHeader(primaryCryptor.id(), metadata)
            SequenceInputStream(cryptorHeader.inputStream(), encryptedData)
        }
    }

    fun decryptStream(encryptedData: InputStream): InputStream {
        val bufferedInputStream = validateStreamAndReturnBuffered(encryptedData)
        return when (val parsedHeader = headerParser.parseDataWithHeader(bufferedInputStream)) {
            ParseResult.NoHeader -> {
                val decryptor = cryptorsForDecryptionOnly.firstOrNull { it.id().contentEquals(LEGACY_CRYPTOR_ID) }
                decryptor?.decryptStream(EncryptedStreamData(stream = bufferedInputStream)) ?: throw PubNubException(
                    errorMessage = "LegacyCryptor not registered",
                    pubnubError = PubNubError.UNKNOWN_CRYPTOR
                )
            }

            is ParseResult.Success -> {
                val decryptor = cryptorsForDecryptionOnly.first {
                    it.id().contentEquals(parsedHeader.cryptoId)
                }
                decryptor.decryptStream(
                    EncryptedStreamData(
                        metadata = parsedHeader.cryptorData,
                        stream = parsedHeader.encryptedData
                    )
                )
            }
        }
    }

    private fun getDecryptedDataForLegacyCryptor(encryptedData: ByteArray): ByteArray {
        return getLegacyCryptor()?.decrypt(EncryptedData(data = encryptedData)) ?: throw PubNubException(
            errorMessage = "LegacyCryptor not available",
            pubnubError = PubNubError.UNKNOWN_CRYPTOR
        )
    }

    private fun getDecryptedDataForCryptorWithHeader(parsedHeader: ParseResult.Success<out ByteArray>): ByteArray {
        val decryptedData: ByteArray
        val cryptorId = parsedHeader.cryptoId
        val cryptorData = parsedHeader.cryptorData
        val pureEncryptedData = parsedHeader.encryptedData
        val cryptor = getCryptorById(cryptorId)
        decryptedData =
            cryptor?.decrypt(EncryptedData(cryptorData, pureEncryptedData))
                ?: throw PubNubException(errorMessage = "No cryptor found", pubnubError = PubNubError.UNKNOWN_CRYPTOR)
        return decryptedData
    }

    private fun getLegacyCryptor(): Cryptor? {
        val idOfLegacyCryptor = ByteArray(4) { 0.toByte() }
        return getCryptorById(idOfLegacyCryptor)
    }

    private fun getCryptorById(cryptorId: ByteArray): Cryptor? {
        return cryptorsForDecryptionOnly.firstOrNull { it.id().contentEquals(cryptorId) }
    }

    private fun validateData(data: ByteArray) {
        if (data.isEmpty()) {
            throw PubNubException(
                errorMessage = "Encryption/Decryption of empty data not allowed.",
                pubnubError = PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED
            )
        }
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
}

internal fun CryptoModule.encryptString(inputString: String): String =
    String(Base64.encode(encrypt(inputString.toByteArray()), Base64.NO_WRAP))

@Throws(PubNubException::class)
internal fun CryptoModule.decryptString(inputString: String): String = try {
    decrypt(Base64.decode(inputString, Base64.NO_WRAP)).toString(Charsets.UTF_8)
} catch (e: IllegalArgumentException) {
    throw PubNubException(e.message, PubNubError.CRYPTO_ERROR, null, 0, null)
}

// this method read data from stream and allows to read them again in subsequent reads without manual reset or repositioning
internal fun BufferedInputStream.checkMinSize(size: Int, exceptionBlock: (Int) -> Unit) {
    mark(size + 1)

    val readBytes = readNBytez(size)
    reset()
    if (readBytes.size < size) {
        exceptionBlock(size)
    }
}

internal fun BufferedInputStream.readExactlyNBytez(size: Int, exceptionBlock: (Int) -> Unit): ByteArray {
    val readBytes = readNBytez(size)
    if (readBytes.size < size) {
        exceptionBlock(size)
    }
    return readBytes
}

internal fun InputStream.readNBytez(len: Int): ByteArray {
    var remaining: Int = len
    var n: Int
    val originalArray = ByteArray(remaining)
    var nread = 0

    while (read(originalArray, nread, min(originalArray.size - nread, remaining)).also { n = it } > 0) {
        nread += n
        remaining -= n
    }
    return originalArray.copyOf(nread)
}
