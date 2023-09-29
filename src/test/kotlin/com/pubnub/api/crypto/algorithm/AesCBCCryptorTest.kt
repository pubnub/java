package com.pubnub.api.crypto.algorithm

import com.pubnub.api.crypto.cryptor.AesCbcCryptor
import com.pubnub.api.crypto.data.EncryptedData
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource

class AesCBCCryptorTest {
    private lateinit var objectUnderTest: AesCbcCryptor

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

    @BeforeEach
    fun setUp() {
        objectUnderTest = AesCbcCryptor("enigma")
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun canDecryptTextWhatIsEncrypted(msgToBeEncrypted: String) {
        // given
        val msgToEncrypt = msgToBeEncrypted.toByteArray()

        // when
        val encryptedMsg = objectUnderTest.encrypt(msgToEncrypt)
        val decryptedMsg = objectUnderTest.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun encryptingTwoTimesTheSameMessageProducesDifferentOutput(msgToBeEncrypted: String) {
        // given
        val msgToEncrypt = msgToBeEncrypted.toByteArray()

        // when
        val encrypted1: EncryptedData = objectUnderTest.encrypt(msgToEncrypt)
        val encrypted2: EncryptedData = objectUnderTest.encrypt(msgToEncrypt)

        // then
        Assertions.assertFalse(encrypted1.data.contentEquals(encrypted2.data))
    }

    @ParameterizedTest
    @MethodSource("messageToBeEncrypted")
    fun encryptingTwoTimesDecryptedMsgIsTheSame(msgToBeEncrypted: String) {
        // given
        val msgToEncrypt = msgToBeEncrypted.toByteArray()

        // when
        val encrypted1 = objectUnderTest.encrypt(msgToEncrypt)
        val encrypted2 = objectUnderTest.encrypt(msgToEncrypt)

        // then
        assertArrayEquals(msgToEncrypt, objectUnderTest.decrypt(encrypted1))
        assertArrayEquals(msgToEncrypt, objectUnderTest.decrypt(encrypted2))
    }
}
