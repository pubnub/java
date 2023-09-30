package com.pubnub.contract

import java.nio.file.Files
import java.nio.file.Paths

fun getFileContentAsByteArray(fileName: String): ByteArray {
    val cryptoFileLocation = CONTRACT_TEST_CONFIG.cryptoFilesLocation()
    return Files.readAllBytes(Paths.get(cryptoFileLocation, fileName))
}
