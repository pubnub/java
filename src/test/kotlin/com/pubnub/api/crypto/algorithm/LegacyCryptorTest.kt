package com.pubnub.api.crypto.algorithm

import com.pubnub.api.crypto.cryptor.LegacyCryptor
import com.pubnub.api.crypto.data.EncryptedData
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class LegacyCryptorTest {

    companion object {
        @JvmStatic
        fun messageToBeEncrypted(): List<Arguments> = listOf(
            Arguments.of(""),
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
}
