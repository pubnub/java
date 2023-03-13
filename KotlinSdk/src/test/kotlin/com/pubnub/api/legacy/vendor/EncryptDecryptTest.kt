package com.pubnub.apikt.legacy.vendor

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.vendor.Crypto
import org.junit.Assert
import org.junit.Test
import java.io.IOException

class EncryptDecryptTest {
    @Test
    @Throws(IOException::class, PubNubException::class)
    fun canDecryptTextWhatIsEncryptedWithStaticIV() {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = "Hello world"

        // when
        val crypto = Crypto(cipherKey)
        val encryptedMsg = crypto.encrypt(msgToEncrypt)
        val decryptedMsg = crypto.decrypt(encryptedMsg)

        // then
        Assert.assertEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    @Throws(IOException::class, PubNubException::class)
    fun canDecryptTextWhatIsEncryptedWithRandomIV() {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = "Hello world"

        // when
        val crypto = Crypto(cipherKey, true)
        val encryptedMsg = crypto.encrypt(msgToEncrypt)
        val decryptedMsg = crypto.decrypt(encryptedMsg)

        // then
        Assert.assertEquals(msgToEncrypt, decryptedMsg)
    }

    @Test
    @Throws(PubNubException::class)
    fun encryptingWithRandomIVTwoTimesTheSameMessageProducesDifferentOutput() {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = "Hello world"

        // when
        val crypto = Crypto(cipherKey, true)
        val encrypted1 = crypto.encrypt(msgToEncrypt)
        val encrypted2 = crypto.encrypt(msgToEncrypt)

        // then
        Assert.assertNotEquals(encrypted1, encrypted2)
    }

    @Test
    @Throws(PubNubException::class)
    fun encryptingWithRandomIVTwoTimesDecryptedMsgIsTheSame() {
        // given
        val cipherKey = "enigma"
        val msgToEncrypt = "Hello world"

        // when
        val crypto = Crypto(cipherKey, true)
        val encrypted1 = crypto.encrypt(msgToEncrypt)
        val encrypted2 = crypto.encrypt(msgToEncrypt)

        // then
        Assert.assertEquals(msgToEncrypt, crypto.decrypt(encrypted1))
        Assert.assertEquals(msgToEncrypt, crypto.decrypt(encrypted2))
    }
}
