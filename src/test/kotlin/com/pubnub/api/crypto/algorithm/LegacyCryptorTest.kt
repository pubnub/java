package com.pubnub.api.crypto.algorithm

import com.pubnub.api.crypto.cryptor.LegacyCryptor
import com.pubnub.api.crypto.data.EncryptedData
import com.pubnub.api.crypto.data.EncryptedStreamData
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.ValueSource
import java.io.ByteArrayInputStream

class LegacyCryptorTest {

    companion object {
        @JvmStatic
        fun messageToBeEncrypted(): List<Arguments> = listOf(
            Arguments.of("Hello world"),
            Arguments.of("Zażółć gęślą jaźń"), // Polish
            Arguments.of("हैलो वर्ल्ड"), // Hindi
            Arguments.of("こんにちは世界"), // Japan
            Arguments.of("你好世界"), // Chinese
        )
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun canDecryptTextWhatIsEncryptedWithStaticIV(messageToBeEncrypted: String) {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = messageToBeEncrypted.toByteArray()

        // when
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = false)
        val encryptedMsg = cryptor.encrypt(msgToEncrypt)
        val decryptedMsg = cryptor.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun canDecryptTextWhatIsEncryptedWithRandomIV(messageToBeEncrypted: String) {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = messageToBeEncrypted.toByteArray()

        // when
        val cryptor = LegacyCryptor(cipherKey = cipherKey)
        val encryptedMsg = cryptor.encrypt(msgToEncrypt)
        val decryptedMsg = cryptor.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun encryptingWithRandomIVTwoTimesTheSameMessageProducesDifferentOutput(messageToBeEncrypted: String) {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = messageToBeEncrypted.toByteArray()

        // when
        val cryptor = LegacyCryptor(cipherKey = cipherKey)
        val encrypted1: EncryptedData = cryptor.encrypt(msgToEncrypt)
        val encrypted2: EncryptedData = cryptor.encrypt(msgToEncrypt)

        // then
        assertFalse(encrypted1.data.contentEquals(encrypted2.data))
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun encryptingWithRandomIVTwoTimesDecryptedMsgIsTheSame(messageToBeEncrypted: String) {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = messageToBeEncrypted.toByteArray()

        // when
        val cryptor = LegacyCryptor(cipherKey = cipherKey)
        val encrypted1 = cryptor.encrypt(msgToEncrypt)
        val encrypted2 = cryptor.encrypt(msgToEncrypt)

        // then
        assertArrayEquals(msgToEncrypt, cryptor.decrypt(encrypted1))
        assertArrayEquals(msgToEncrypt, cryptor.decrypt(encrypted2))
    }

    @ParameterizedTest
    @ValueSource(booleans = [true, false])
    fun `should throw exception when encrypting empty data`(useRandomIv: Boolean) {
        // given
        val msgToEncrypt = "".toByteArray()
        val cipherKey = "enigma"
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = useRandomIv)

        // when
        val exception = Assertions.assertThrows(PubNubException::class.java) {
            cryptor.encrypt(msgToEncrypt)
        }

        // then
        Assertions.assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        Assertions.assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @Test
    fun `should throw exception when decrypting data containing only initialization vector and cryptor has randomIv`() {
        // given
        val msgToDecrypt = ByteArray(16) { it.toByte() } // IV has 16 bytes
        val encryptedData = EncryptedData(data = msgToDecrypt)
        val cipherKey = "enigma"
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = true)

        // when
        val exception = Assertions.assertThrows(PubNubException::class.java) {
            cryptor.decrypt(encryptedData)
        }

        // then
        Assertions.assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        Assertions.assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @Test
    fun `should throw exception when decrypting empty data and cryptor has staticIv`() {
        // given
        val msgToDecrypt = "".toByteArray()
        val encryptedData = EncryptedData(data = msgToDecrypt)
        val cipherKey = "enigma"
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = false)

        // when
        val exception = Assertions.assertThrows(PubNubException::class.java) {
            cryptor.decrypt(encryptedData)
        }

        // then
        Assertions.assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        Assertions.assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @Test
    fun `should throw exception when encrypting empty stream`() {
        // given
        val msgToEncrypt = ""
        val streamToEncrypt = msgToEncrypt.byteInputStream()
        val cipherKey = "enigma"
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = false)

        // when
        val exception = Assertions.assertThrows(PubNubException::class.java) {
            cryptor.encryptStream(streamToEncrypt)
        }

        // then
        Assertions.assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        Assertions.assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @Test
    fun `should throw exception when decrypting empty stream`() {
        // given
        val msgToDecrypt = ByteArray(16) { it.toByte() } // IV has 16 bytes
        val streamToEncrypt = ByteArrayInputStream(msgToDecrypt)
        val encryptedStreamData = EncryptedStreamData(stream = streamToEncrypt)
        val cipherKey = "enigma"
        val cryptor = LegacyCryptor(cipherKey = cipherKey, useRandomIv = false)

        // when
        val exception = Assertions.assertThrows(PubNubException::class.java) {
            cryptor.decryptStream(encryptedStreamData)
        }

        // then
        Assertions.assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        Assertions.assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }
}
