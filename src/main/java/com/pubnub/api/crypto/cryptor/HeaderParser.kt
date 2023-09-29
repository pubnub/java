package com.pubnub.api.crypto.cryptor

import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import org.slf4j.LoggerFactory
import java.io.BufferedInputStream
import java.io.InputStream

private val SENTINEL = "PNED".toByteArray()
private const val STARTING_INDEX_OF_ONE_BYTE_CRYPTOR_DATA_SIZE = 10
private const val STARTING_INDEX_OF_THREE_BYTES_CRYPTOR_DATA_SIZE = 12
private const val MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER = 10
private const val THREE_BYTES_SIZE_CRYPTOR_DATA_INDICATOR: UByte = 255U

private const val SENTINEL_STARTING_INDEX = 0
private const val SENTINEL_ENDING_INDEX = 3
private const val VERSION_INDEX = 4
private const val CRYPTOR_ID_STARTING_INDEX = 5
private const val CRYPTOR_ID_ENDING_INDEX = 8
private const val CRYPTOR_DATA_SIZE_STARTING_INDEX = 9
private const val THREE_BYTES_CRYPTOR_DATA_SIZE_STARTING_INDEX = 10
private const val THREE_BYTES_CRYPTOR_DATA_SIZE_ENDING_INDEX = 11
private const val MAX_VALUE_THAT_CAN_BE_STORED_ON_TWO_BYTES = 65535

class HeaderParser {
    private val log = LoggerFactory.getLogger(HeaderParser::class.java)

    fun parseDataWithHeader(stream: BufferedInputStream): ParseResult<out InputStream> {
        val bufferedInputStream = stream.buffered()
        bufferedInputStream.mark(Int.MAX_VALUE) // TODO Can be calculated from spec
        val possibleInitialHeader = ByteArray(MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER)
        val initiallyRead = bufferedInputStream.read(possibleInitialHeader)
        if (!possibleInitialHeader.sliceArray(SENTINEL_STARTING_INDEX..SENTINEL_ENDING_INDEX).contentEquals(SENTINEL)) {
            bufferedInputStream.reset()
            return ParseResult.NoHeader
        }

        if (initiallyRead < MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER) {
            throw PubNubException(errorMessage = "Minimal size of Cryptor Data Header is: $MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER")
        }

        validateCryptorHeaderVersion(possibleInitialHeader)
        val cryptorId = possibleInitialHeader.sliceArray(CRYPTOR_ID_STARTING_INDEX..CRYPTOR_ID_ENDING_INDEX)
        val cryptorDataSizeFirstByte = possibleInitialHeader[CRYPTOR_DATA_SIZE_STARTING_INDEX].toUByte()

        val cryptorData: ByteArray = if (cryptorDataSizeFirstByte == THREE_BYTES_SIZE_CRYPTOR_DATA_INDICATOR) {
            val cryptorDataSizeBytes = bufferedInputStream.readNBytez(2)
            val cryptorDataSize = convertTwoBytesToIntBigEndian(cryptorDataSizeBytes[0], cryptorDataSizeBytes[1])
            bufferedInputStream.readNBytez(cryptorDataSize)
        } else {
            if (cryptorDataSizeFirstByte == UByte.MIN_VALUE) {
                byteArrayOf()
            } else {
                bufferedInputStream.readNBytez(cryptorDataSizeFirstByte.toInt())
            }
        }
        return ParseResult.Success(cryptorId, cryptorData, bufferedInputStream)
    }

    fun InputStream.readNBytez(n: Int): ByteArray {
        val cryptorData = ByteArray(n)
        val numberOfReadBytes = read(cryptorData)
        if (numberOfReadBytes != n) {
            throw PubNubException(errorMessage = "Couldn't read $n bytes")
        }
        return cryptorData
    }

    fun parseDataWithHeader(data: ByteArray): ParseResult<out ByteArray> {

        val sentinel = data.sliceArray(SENTINEL_STARTING_INDEX..SENTINEL_ENDING_INDEX)
        if (!SENTINEL.contentEquals(sentinel)) {
            return ParseResult.NoHeader
        }

        if (data.size < MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER) {
            throw PubNubException(
                errorMessage = "Minimal size of Cryptor Data Header is: $MINIMAL_SIZE_OF_DATA_HAVING_CRYPTOR_HEADER",
                pubnubError = PubNubError.CRYPTOR_DATA_HEADER_SIZE_TO_SMALL
            )
        }

        validateCryptorHeaderVersion(data)

        val cryptorId = data.sliceArray(CRYPTOR_ID_STARTING_INDEX..CRYPTOR_ID_ENDING_INDEX)
        log.trace("CryptoId: ${String(cryptorId, Charsets.UTF_8)}")

        val cryptorDataSizeFirstByte: Byte = data[CRYPTOR_DATA_SIZE_STARTING_INDEX]
        val (startingIndexOfCryptorData, cryptorDataSize) = getCryptorDataSizeAndStartingIndex(
            data,
            cryptorDataSizeFirstByte
        )

        if (startingIndexOfCryptorData + cryptorDataSize > data.size) {
            throw PubNubException(errorMessage = "Input data size: ${data.size} is to small to fit header of size $startingIndexOfCryptorData and cryptorData of size: $cryptorDataSize")
        }
        val cryptorData =
            data.sliceArray(startingIndexOfCryptorData until (startingIndexOfCryptorData + cryptorDataSize))
        val sizeOfCryptorHeader = startingIndexOfCryptorData + cryptorDataSize
        val encryptedData = data.sliceArray(sizeOfCryptorHeader until data.size)

        return ParseResult.Success(cryptorId, cryptorData, encryptedData)
    }

    fun createCryptorHeader(cryptorId: ByteArray, cryptorData: ByteArray?): ByteArray {
        val sentinel: ByteArray = SENTINEL
        val cryptorHeaderVersion: Byte = getCurrentCryptoHeaderVersion()
        val cryptorDataSize: Int = cryptorData?.size ?: 0
        val finalCryptorDataSize: ByteArray =
            if (cryptorDataSize < THREE_BYTES_SIZE_CRYPTOR_DATA_INDICATOR.toInt()) {
                byteArrayOf(cryptorDataSize.toByte()) // cryptorDataSize will be stored on 1 byte
            } else if (cryptorDataSize < MAX_VALUE_THAT_CAN_BE_STORED_ON_TWO_BYTES) {
                byteArrayOf(cryptorDataSize.toByte()) + writeNumberOnTwoBytes(cryptorDataSize) // cryptorDataSize will be stored on 3 byte
            } else {
                throw PubNubException(errorMessage = "Cryptor Data Size is: $cryptorDataSize whereas max cryptor data size is: $MAX_VALUE_THAT_CAN_BE_STORED_ON_TWO_BYTES ")
            }

        val cryptorHeader =
            CryptorHeader(sentinel, cryptorHeaderVersion, cryptorId, finalCryptorDataSize, cryptorData ?: byteArrayOf())
        return cryptorHeader.toByteArray()
    }

    private fun getCurrentCryptoHeaderVersion(): Byte {
        return CryptorHeaderVersion.One.value.toByte()
    }

    private fun getCryptorDataSizeAndStartingIndex(data: ByteArray, cryptorDataSizeFirstByte: Byte): Pair<Int, Int> {
        val startingIndexOfCryptorData: Int
        val cryptorDataSize: Int
        val cryptoDataFirstByteAsUByte: UByte = cryptorDataSizeFirstByte.toUByte()

        if (cryptoDataFirstByteAsUByte == THREE_BYTES_SIZE_CRYPTOR_DATA_INDICATOR) {
            startingIndexOfCryptorData = STARTING_INDEX_OF_THREE_BYTES_CRYPTOR_DATA_SIZE
            log.trace("\"Cryptor data size\" first byte's value is 255 that mean that size is stored on two next bytes")
            val cryptorDataSizeSecondByte = data[THREE_BYTES_CRYPTOR_DATA_SIZE_STARTING_INDEX]
            val cryptorDataSizeThirdByte = data[THREE_BYTES_CRYPTOR_DATA_SIZE_ENDING_INDEX]
            cryptorDataSize = convertTwoBytesToIntBigEndian(cryptorDataSizeSecondByte, cryptorDataSizeThirdByte)
        } else {
            startingIndexOfCryptorData = STARTING_INDEX_OF_ONE_BYTE_CRYPTOR_DATA_SIZE
            cryptorDataSize = cryptoDataFirstByteAsUByte.toInt()
            log.trace("\"Cryptor data size\" is 1 byte long and its value is: $cryptorDataSize")
        }
        return Pair(startingIndexOfCryptorData, cryptorDataSize)
    }

    private fun validateCryptorHeaderVersion(data: ByteArray) {
        val version: UByte = data[VERSION_INDEX].toUByte() // 5th byte
        val versionAsInt = version.toInt()
        log.trace("Cryptor header version is: $versionAsInt")
        // check if version exist in this SDK version
        CryptorHeaderVersion.fromValue(versionAsInt)
            ?: throw PubNubException(
                errorMessage = "Cryptor header version unknown. Please, update SDK",
                pubnubError = PubNubError.CRYPTOR_HEADER_VERSION_UNKNOWN
            )
    }

    private fun convertTwoBytesToIntBigEndian(byte1: Byte, byte2: Byte): Int {
        return ((byte1.toInt() and 0xFF) shl 8) or (byte2.toInt() and 0xFF)
    }

    private fun writeNumberOnTwoBytes(number: Int): ByteArray {
        val result = ByteArray(2)

        result[0] = (number shr 8).toByte()
        result[1] = number.toByte()

        return result
    }
}

sealed class ParseResult<T> {
    data class Success<T>(val cryptoId: ByteArray, val cryptorData: ByteArray, val encryptedData: T) :
        ParseResult<T>()

    object NoHeader : ParseResult<Nothing>()
}
