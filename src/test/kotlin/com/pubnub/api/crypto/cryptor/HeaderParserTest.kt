package com.pubnub.api.crypto.cryptor

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HeaderParserTest {
    private lateinit var objectUnderTest: HeaderParser

    @BeforeEach
    fun setUp() {
        objectUnderTest = HeaderParser()
    }

    @Test
    fun `can create and parse data with header when cryptorDataSize is 1`() {
        val cryptorId: ByteArray =
            byteArrayOf('C'.code.toByte(), 'R'.code.toByte(), 'I'.code.toByte(), 'V'.code.toByte()) // "CRIV"

        val cryptorData = byteArrayOf(0x50)
        val cryptorHeader = objectUnderTest.createCryptorHeader(cryptorId, cryptorData)

        val dataToBeEncrypted = byteArrayOf('D'.code.toByte(), 'A'.code.toByte())
        val headerWithData: ByteArray = cryptorHeader + dataToBeEncrypted
        val parseResult = objectUnderTest.parseDataWithHeader(headerWithData)

        when (parseResult) {
            is ParseResult.NoHeader -> fail("Expected header")
            is ParseResult.Success -> {
                assertTrue(cryptorId.contentEquals(parseResult.cryptoId))
                assertTrue(cryptorData.contentEquals(parseResult.cryptorData))
                assertTrue(dataToBeEncrypted.contentEquals(parseResult.encryptedData))
            }
        }
    }

    @Test
    internal fun `can create and parse data with header when cryptorDataSize is 3`() {
        val cryptorId: ByteArray =
            byteArrayOf('C'.code.toByte(), 'R'.code.toByte(), 'I'.code.toByte(), 'V'.code.toByte()) // "CRIV"
        val cryptorData = createByteArrayThatHas255Elements()
        val cryptorHeader = objectUnderTest.createCryptorHeader(cryptorId, cryptorData)

        val dataToBeEncrypted = byteArrayOf('D'.code.toByte(), 'A'.code.toByte())
        val headerWithData: ByteArray = cryptorHeader + dataToBeEncrypted
        val parseResult = objectUnderTest.parseDataWithHeader(headerWithData)

        when (parseResult) {
            is ParseResult.NoHeader -> fail("Expected header")
            is ParseResult.Success -> {
                assertTrue(cryptorId.contentEquals(parseResult.cryptoId))
                assertTrue(cryptorData.contentEquals(parseResult.cryptorData))
                assertTrue(dataToBeEncrypted.contentEquals(parseResult.encryptedData))
            }
        }
    }

    @Test
    fun `should return NoHeader when there is no sentinel`() {
        val cryptorHeaderWithInvalidSentinel =
            byteArrayOf(0x56, 0x56, 0x56, 0x56, 0x01, 0x43, 0x52, 0x49, 0x56, 0x10, 0x10)
        val parseResult = objectUnderTest.parseDataWithHeader(cryptorHeaderWithInvalidSentinel)

        assertThat(parseResult, `is`(ParseResult.NoHeader))
    }

    private fun createByteArrayThatHas255Elements(): ByteArray {
        var byteArray: ByteArray = byteArrayOf()
        for (i in 1..255) {
            byteArray += byteArrayOf(i.toByte())
        }
        return byteArray
    }
}
