package com.pubnub.api.crypto

import com.pubnub.api.crypto.cryptor.AesCbcCryptor
import com.pubnub.api.crypto.cryptor.Cryptor
import com.pubnub.api.crypto.cryptor.LegacyCryptor
import com.pubnub.api.crypto.data.EncryptedData
import com.pubnub.api.crypto.data.EncryptedStreamData
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.hasSize
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*
import org.hamcrest.Matchers.`is` as iz

class CryptoModuleTest {

    @Test
    fun `can createLegacyCryptoModule`() {
        // given
        val cipherKey = "enigma"

        // when
        val legacyCryptoModule = CryptoModule.createLegacyCryptoModule(cipherKey)

        // then
        assertTrue(legacyCryptoModule.primaryCryptor is LegacyCryptor)
        assertThat(legacyCryptoModule.cryptorsForDecryptionOnly, hasSize(2))
        assertThat(
            legacyCryptoModule.cryptorsForDecryptionOnly,
            containsInAnyOrder(
                listOf(
                    iz(CoreMatchers.instanceOf(AesCbcCryptor::class.java)),
                    iz(CoreMatchers.instanceOf(LegacyCryptor::class.java))
                )
            )
        )
    }

    @Test
    fun `can createAesCbcCryptoModule`() {
        // given
        val cipherKey = "enigma"

        // when
        val aesCbcCryptoModule = CryptoModule.createAesCbcCryptoModule(cipherKey)

        // then
        assertTrue(aesCbcCryptoModule.primaryCryptor is AesCbcCryptor)
        assertThat(aesCbcCryptoModule.cryptorsForDecryptionOnly, hasSize(2))
        assertThat(
            aesCbcCryptoModule.cryptorsForDecryptionOnly,
            containsInAnyOrder(
                listOf(
                    iz(CoreMatchers.instanceOf(AesCbcCryptor::class.java)),
                    iz(CoreMatchers.instanceOf(LegacyCryptor::class.java))
                )
            )
        )
    }

    @Test
    fun `can createNewCryptoModule`() {
        // given
        val cipherKey = "enigma"

        // when
        val newCryptoModule = CryptoModule.createNewCryptoModule(defaultCryptor = AesCbcCryptor(cipherKey))

        // then
        assertTrue(newCryptoModule.primaryCryptor is AesCbcCryptor)
        assertThat(newCryptoModule.cryptorsForDecryptionOnly, hasSize(1))
        assertThat(
            newCryptoModule.cryptorsForDecryptionOnly.first(),
            iz(CoreMatchers.instanceOf(AesCbcCryptor::class.java))
        )
    }

    @Test
    fun `can decrypt encrypted message using LegacyCryptoModule with randomIV`() {
        // given
        val cipherKey = "enigma"
        val legacyCryptoModuleWithRandomIv = CryptoModule.createLegacyCryptoModule(cipherKey)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = legacyCryptoModuleWithRandomIv.encrypt(msgToEncrypt)
        val decryptedMsg = legacyCryptoModuleWithRandomIv.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `can decrypt encrypted message using LegacyCryptoModule with staticIV`() {
        // given
        val cipherKey = "enigma"
        val legacyCryptoModuleWithStaticIv = CryptoModule.createLegacyCryptoModule(cipherKey, false)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = legacyCryptoModuleWithStaticIv.encrypt(msgToEncrypt)
        val decryptedMsg = legacyCryptoModuleWithStaticIv.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `using LegacyCryptoModule can decrypt message that was encrypted with AesCbcCryptor`() {
        // given
        val cipherKey = "enigma"
        val moduleWithAesCbcCryptorOnly = CryptoModule.createNewCryptoModule(defaultCryptor = AesCbcCryptor(cipherKey))
        val legacyCryptoModule = CryptoModule.createLegacyCryptoModule(cipherKey)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = moduleWithAesCbcCryptorOnly.encrypt(msgToEncrypt)
        val decryptedMsg = legacyCryptoModule.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `using AesCbcCryptoModule can decrypt message that was encrypted with LegacyCryptor with randomIV `() {
        // given
        val cipherKey = "enigma"
        val moduleWithLegacyCryptorOnlyWithRandomIV =
            CryptoModule.createNewCryptoModule(defaultCryptor = LegacyCryptor(cipherKey))
        val aesCbcCryptoModule = CryptoModule.createAesCbcCryptoModule(cipherKey)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = moduleWithLegacyCryptorOnlyWithRandomIV.encrypt(msgToEncrypt)
        val decryptedMsg = aesCbcCryptoModule.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `using AesCbcCryptoModule can decrypt message that was encrypted with LegacyCryptor with staticIV `() {
        // given
        val cipherKey = "enigma"
        val moduleWithLegacyCryptorOnlyWithStaticIV =
            CryptoModule.createNewCryptoModule(defaultCryptor = LegacyCryptor(cipherKey, false))
        val aesCbcCryptoModule = CryptoModule.createAesCbcCryptoModule(cipherKey, false)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = moduleWithLegacyCryptorOnlyWithStaticIV.encrypt(msgToEncrypt)
        val decryptedMsg = aesCbcCryptoModule.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `can decrypt encrypted message using module with only AesCbcCryptor`() {
        // given
        val cipherKey = "enigma"
        val aesCbcCryptoModule = CryptoModule.createAesCbcCryptoModule(cipherKey)
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = aesCbcCryptoModule.encrypt(msgToEncrypt)
        val decryptedMsg = aesCbcCryptoModule.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `can add the same module as a defaultCryptor and cryptorsForDecryptionOnly and have decryption working properly`() {
        // given
        val cipherKey = "enigma"
        val legacyCryptor = LegacyCryptor(cipherKey)
        val cryptoModule = CryptoModule.createNewCryptoModule(
            defaultCryptor = legacyCryptor,
            cryptorsForDecryptionOnly = listOf(legacyCryptor, AesCbcCryptor(cipherKey))
        )
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = cryptoModule.encrypt(msgToEncrypt)
        val decryptedMsg = cryptoModule.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    fun `can decrypt encrypted message using custom cryptor `() {
        // given
        val customCryptor = myCustomCryptor()
        val msgToEncrypt = "Hello world".toByteArray()

        // when
        val encryptedMsg = customCryptor.encrypt(msgToEncrypt)
        val decryptedMsg = customCryptor.decrypt(encryptedMsg)

        // then
        assertArrayEquals(msgToEncrypt, decryptedMsg)
    }

    @ParameterizedTest
    @MethodSource("legacyAndAesCbcCryptors")
    fun `should throw exception when encrypting empty data`(cryptoModule: CryptoModule) {
        // given
        val dataToBeEncrypted = ByteArray(0)

        // when
        val exception = assertThrows(PubNubException::class.java) {
            cryptoModule.encrypt(dataToBeEncrypted)
        }

        // then
        assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @ParameterizedTest
    @MethodSource("legacyAndAesCbcCryptors")
    fun `should throw exception when decrypting empty data`(cryptoModule: CryptoModule) {
        // given
        val dataToBeDecrypted = ByteArray(0)

        // when
        val exception = assertThrows(PubNubException::class.java) {
            cryptoModule.decrypt(dataToBeDecrypted)
        }

        // then
        assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @ParameterizedTest
    @MethodSource("legacyAndAesCbcCryptors")
    fun `should throw exception when encrypting empty stream`(cryptoModule: CryptoModule) {
        // given
        val dataToBeEncrypted = ByteArray(0)
        val streamToBeEncrypted = ByteArrayInputStream(dataToBeEncrypted)

        // when
        val exception = assertThrows(PubNubException::class.java) {
            cryptoModule.encryptStream(streamToBeEncrypted)
        }

        // then
        assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    @ParameterizedTest
    @MethodSource("legacyAndAesCbcCryptors")
    fun `should throw exception when decrypting empty stream`(cryptoModule: CryptoModule) {
        // given
        val dataToBeDecrypted = ByteArray(0)
        val streamToBeDecrypted = ByteArrayInputStream(dataToBeDecrypted)

        // when
        val exception = assertThrows(PubNubException::class.java) {
            cryptoModule.decryptStream(streamToBeDecrypted)
        }

        // then
        assertEquals("Encryption/Decryption of empty data not allowed.", exception.errorMessage)
        assertEquals(PubNubError.ENCRYPTION_AND_DECRYPTION_OF_EMPTY_DATA_NOT_ALLOWED, exception.pubnubError)
    }

    private fun myCustomCryptor() = object : Cryptor {
        override fun id(): ByteArray {
            return byteArrayOf('C'.code.toByte(), 'U'.code.toByte(), 'S'.code.toByte(), 'T'.code.toByte())
        }

        override fun encrypt(data: ByteArray): EncryptedData {
            return EncryptedData(metadata = null, data = data)
        }

        override fun decrypt(encryptedData: EncryptedData): ByteArray {
            return encryptedData.data
        }

        override fun encryptStream(stream: InputStream): EncryptedStreamData {
            throw NotImplementedError()
        }

        override fun decryptStream(encryptedData: EncryptedStreamData): InputStream {
            throw NotImplementedError()
        }
    }

    @ParameterizedTest
    @MethodSource("decryptStreamSource")
    fun decryptStreamEncryptedByGo(expected: String, encryptedBase64: String, cipherKey: String) {
        val crypto = CryptoModule.createLegacyCryptoModule(cipherKey, true)
        val decrypted = crypto.decryptStream(Base64.getDecoder().decode(encryptedBase64).inputStream())
        assertEquals(expected, String(decrypted.readBytes()))
    }

    @ParameterizedTest
    @MethodSource("encryptStreamDecryptStreamSource")
    fun encryptStreamDecryptStream(input: String, cryptoModule: CryptoModule) {
        val encrypted = cryptoModule.encryptStream(input.byteInputStream())
        val decrypted = cryptoModule.decryptStream(encrypted)
        assertEquals(input, String(decrypted.readBytes()))
    }

    companion object {
        @JvmStatic
        fun decryptStreamSource(): List<Arguments> = listOf(
            Arguments.of(
                "Hello world encrypted with legacyModuleRandomIv",
                "T3J9iXI87PG9YY/lhuwmGRZsJgA5y8sFLtUpdFmNgrU1IAitgAkVok6YP7lacBiVhBJSJw39lXCHOLxl2d98Bg==",
                "myCipherKey",

            ),
            Arguments.of(
                "Hello world encrypted with aesCbcModule",
                "UE5FRAFBQ1JIEKzlyoyC/jB1hrjCPY7zm+X2f7skPd0LBocV74cRYdrkRQ2BPKeA22gX/98pMqvcZtFB6TCGp3Zf1M8F730nlfk=",
                "myCipherKey"

            ),
        )

        @JvmStatic
        fun encryptStreamDecryptStreamSource(): List<Arguments> = listOf(
            Arguments.of("Hello world1", CryptoModule.createLegacyCryptoModule("myCipherKey", true)),
            Arguments.of("Hello world2", CryptoModule.createLegacyCryptoModule("myCipherKey", false)),
            Arguments.of("Hello world3", CryptoModule.createAesCbcCryptoModule("myCipherKey", true)),
            Arguments.of("Hello world4", CryptoModule.createAesCbcCryptoModule("myCipherKey", false)),
        )

        @JvmStatic
        fun legacyAndAesCbcCryptors(): List<Arguments> = listOf(
            Arguments.of(CryptoModule.createLegacyCryptoModule("myCipherKey", true)),
            Arguments.of(CryptoModule.createLegacyCryptoModule("myCipherKey", false)),
            Arguments.of(CryptoModule.createAesCbcCryptoModule("myCipherKey", true)),
            Arguments.of(CryptoModule.createAesCbcCryptoModule("myCipherKey", false)),
        )
    }
}
