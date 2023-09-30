package com.pubnub.contract.crypto

import com.pubnub.api.crypto.CryptoModule
import com.pubnub.api.crypto.cryptor.AesCbcCryptor
import com.pubnub.api.crypto.cryptor.Cryptor
import com.pubnub.api.crypto.cryptor.LegacyCryptor
import com.pubnub.api.crypto.exception.PubNubError
import com.pubnub.api.crypto.exception.PubNubException
import com.pubnub.api.vendor.Base64
import com.pubnub.api.vendor.Crypto
import com.pubnub.contract.getFileContentAsByteArray
import io.cucumber.java.en.Given
import io.cucumber.java.en.Then
import io.cucumber.java.en.When
import org.junit.jupiter.api.Assertions.assertArrayEquals
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue

private const val LEGACY_NEW = "legacy"
private const val AES_CBC = "acrh"
private const val RANDOM_IV = "random"
private const val CRYPTION_TYPE_BINARY = "binary" // not a stream
private const val CRYPTION_TYPE_STREAM = "stream"

class CryptoModuleSteps(
    private val cryptoModuleState: CryptoModuleState,
) {

    @Given("Crypto module with {string} cryptor")
    fun crypto_module_with_cryptor(cryptorType: String) {
        cryptoModuleState.defaultCryptorType = cryptorType
    }

    @Given("with {string} cipher key")
    fun cryptor_with_cipher_key(cipherKey: String) {
        cryptoModuleState.cryptorCipherKey = cipherKey
    }

    @Given("with {string} vector")
    fun cryptor_with_initialization_vector(initializationVectorType: String) {
        cryptoModuleState.initializationVectorType = initializationVectorType
    }

    @Given("Legacy code with {string} cipher key and {string} vector")
    fun legacy_code_with_cipher_key_and_vector(cipherKey: String, initializationVectorType: String) {
        // this is fine, nothing here
    }

    @Given("Crypto module with default {string} and additional {string} cryptors")
    fun crypto_module_with_default_cryptor_and_additional_cryptor(
        defaultCryptorType: String,
        decryptionCryptorType: String
    ) {
        cryptoModuleState.defaultCryptorType = defaultCryptorType
        cryptoModuleState.decryptionOnlyCryptorType = decryptionCryptorType
    }

    @When("I decrypt {string} file")
    fun I_decrypt_file(fileName: String) {
        val encryptedFileContent = getFileContentAsByteArray(fileName)
        var cryptoModule: CryptoModule? = null
        if (cryptoModuleState.defaultCryptorType == AES_CBC) {
            cryptoModule = CryptoModule.createNewCryptoModule(AesCbcCryptor(cryptoModuleState.cryptorCipherKey!!))
        } else if (cryptoModuleState.defaultCryptorType == LEGACY_NEW) {
            cryptoModule = CryptoModule.createNewCryptoModule(LegacyCryptor(cryptoModuleState.cryptorCipherKey!!))
        }

        try {
            cryptoModule?.decrypt(encryptedData = encryptedFileContent)
        } catch (e: PubNubException) {
            cryptoModuleState.decryptionError = e.pubnubError
        }
    }

    @When("I encrypt {string} file as {string}")
    fun I_encrypt_file(fileName: String, encryptionType: String) {
        val notEncryptedFileContent = getFileContentAsByteArray(fileName)
        cryptoModuleState.fileContent = notEncryptedFileContent
        val randoIv: Boolean = cryptoModuleState.initializationVectorType == RANDOM_IV
        val cryptoModule =
            CryptoModule.createNewCryptoModule(LegacyCryptor(cryptoModuleState.cryptorCipherKey!!, randoIv))

        val encryptedData: ByteArray = when (encryptionType) {
            CRYPTION_TYPE_BINARY -> cryptoModule.encrypt(notEncryptedFileContent)
            CRYPTION_TYPE_STREAM -> cryptoModule.encryptStream(notEncryptedFileContent.inputStream()).readBytes()
            else -> throw PubNubException("Invalid encryptionType type. Should be binary or stream")
        }
        cryptoModuleState.encryptedData = encryptedData
    }

    @When("I decrypt {string} file as {string}")
    fun I_decrypt_file_as_binary(encryptedFile: String, decryptionType: String) {
        val defaultCryptorType = cryptoModuleState.defaultCryptorType
        val cipherKey = cryptoModuleState.cryptorCipherKey!!
        val randoIv: Boolean = cryptoModuleState.initializationVectorType == RANDOM_IV
        val cryptoModule: CryptoModule
        if (cryptoModuleState.decryptionOnlyCryptorType == null) {
            cryptoModule = when (defaultCryptorType) {
                LEGACY_NEW -> {
                    CryptoModule.createNewCryptoModule(LegacyCryptor(cipherKey, randoIv))
                }
                AES_CBC -> {
                    CryptoModule.createNewCryptoModule(AesCbcCryptor(cipherKey))
                }
                else -> throw PubNubException("Invalid cryptor type")
            }
        } else {
            val decryptionOnlyCryptorType = cryptoModuleState.decryptionOnlyCryptorType
            val defaultCryptor = createCryptor(defaultCryptorType!!, cipherKey, randoIv)
            val decryptionOnlyCryptor = createCryptor(decryptionOnlyCryptorType!!, cipherKey, randoIv)
            cryptoModule = CryptoModule.createNewCryptoModule(defaultCryptor, listOf(decryptionOnlyCryptor))
        }

        val encryptedFileContent = getFileContentAsByteArray(encryptedFile)

        val decryptedData: ByteArray = when (decryptionType) {
            CRYPTION_TYPE_BINARY -> cryptoModule.decrypt(encryptedFileContent)
            CRYPTION_TYPE_STREAM -> cryptoModule.decryptStream(encryptedFileContent.inputStream()).readBytes()
            else -> throw PubNubException("Invalid decryptionType type. Should be binary or stream")
        }
        cryptoModuleState.decryptedData = decryptedData
    }

    private fun createCryptor(cryptorType: String, cipherKey: String, useRandomIv: Boolean): Cryptor {
        return when (cryptorType) {
            LEGACY_NEW -> {
                LegacyCryptor(cipherKey, useRandomIv)
            }
            AES_CBC -> {
                AesCbcCryptor(cipherKey)
            }
            else -> {
                throw PubNubException("Invalid cryptor type")
            }
        }
    }

    @Then("I receive {string}")
    fun I_receive_outcome(outcome: String) {
        when (outcome) {
            "unknown cryptor error" -> {
                assertTrue(cryptoModuleState.decryptionError == PubNubError.UNKNOWN_CRYPTOR || cryptoModuleState.decryptionError == PubNubError.CRYPTOR_HEADER_VERSION_UNKNOWN)
            }
            "decryption error" -> assertEquals(
                PubNubError.CRYPTOR_DATA_HEADER_SIZE_TO_SMALL,
                cryptoModuleState.decryptionError
            )
            "success" -> assertNull(cryptoModuleState.decryptionError)
        }
    }

    @Then("Successfully decrypt an encrypted file with legacy code")
    fun successfully_decrypt_an_encrypted_file_with_legacy_code() {
        val encryptedData = cryptoModuleState.encryptedData
        val encryptedDataAsStringBase64 = String(Base64.encode(encryptedData, Base64.NO_WRAP))
        val randoIv: Boolean = cryptoModuleState.initializationVectorType == RANDOM_IV
        val crypto = Crypto(cryptoModuleState.cryptorCipherKey, randoIv)
        val decryptedDataAsString: String = crypto.decrypt(encryptedDataAsStringBase64)
        assertEquals(String(cryptoModuleState.fileContent!!), decryptedDataAsString)
    }

    @Then("Decrypted file content equal to the {string} file content")
    fun decrypted_file_content_equal_to_the_source_file_content(sourceFileName: String) {
        val sourceFileContent = getFileContentAsByteArray(sourceFileName)
        assertArrayEquals(sourceFileContent, cryptoModuleState.decryptedData)
    }
}
