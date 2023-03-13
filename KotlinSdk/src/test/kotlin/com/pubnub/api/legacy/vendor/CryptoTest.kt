package com.pubnub.apikt.legacy.vendor

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.vendor.FileEncryptionUtil.decrypt
import com.pubnub.apikt.vendor.FileEncryptionUtil.encryptToBytes
import org.apache.commons.io.IOUtils
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class CryptoTest {
    @Test
    @Throws(IOException::class, PubNubException::class)
    fun canDecryptWhatIsEncrypted() {
        // given
        val cipherKey = "enigma"
        val byteArrayToEncrypt = byteArrayToEncrypt()

        // when
        val encryptedByteArray = encryptToBytes(
            byteArrayToEncrypt,
            cipherKey
        )

        val decryptedByteArray: ByteArray = decrypt(
            ByteArrayInputStream(encryptedByteArray),
            cipherKey
        ).use { decryptedInputStream ->
            ByteArrayOutputStream().use { byteArrayOutputStream ->
                IOUtils.copy(decryptedInputStream, byteArrayOutputStream)
                byteArrayOutputStream.toByteArray()
            }
        }

        // then
        assertThat(
            decryptedByteArray,
            Matchers.allOf(
                Matchers.equalTo(byteArrayToEncrypt),
                Matchers.not(Matchers.equalTo(encryptedByteArray))
            )
        )
    }

    @Test
    fun t() {
        val aaa = "736"
        val ins = aaa.byteInputStream()
        val byteArray = ByteArray(1)
        ins.read(byteArray, 0, byteArray.size)

        val outs = ByteArrayOutputStream().use {
            ins.copyTo(it)
            it
        }
        println(String(byteArray))
        println(String(outs.toByteArray()))
    }

    private fun byteArrayToEncrypt(): ByteArray {
        val random = Random()
        val fileSize = random.nextInt(MAX_FILE_SIZE_IN_BYTES)
        val fileContents = ByteArray(fileSize)
        random.nextBytes(fileContents)
        return fileContents
    }

    companion object {
        private const val MAX_FILE_SIZE_IN_BYTES = 1024 * 1024 * 5
    }
}
