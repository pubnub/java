package com.pubnub.contract.crypto

import com.pubnub.api.crypto.exception.PubNubError

class CryptoModuleState {
    var defaultCryptorType: String? = null
    var decryptionOnlyCryptorType: String? = null
    var cryptorCipherKey: String? = null
    var initializationVectorType: String? = null
    var decryptionError: PubNubError? = null
    var encryptionError: PubNubError? = null
    var encryptedData: ByteArray? = null
    var decryptedData: ByteArray? = null
    var fileContent: ByteArray? = null
    var encryptionType: String? = null
}
