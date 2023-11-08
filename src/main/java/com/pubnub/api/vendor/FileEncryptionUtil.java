package com.pubnub.api.vendor;

import com.pubnub.api.PubNub;
import com.pubnub.api.crypto.CryptoModule;
import lombok.Data;

public final class FileEncryptionUtil {
    private static final int IV_SIZE_BYTES = 16;
    public static final int BUFFER_SIZE_BYTES = 8192;
    static final String ENCODING_UTF_8 = "UTF-8";
    static final String CIPHER_TRANSFORMATION = "AES/CBC/PKCS5Padding";

    @Data
    private static class IvAndData {
        final byte[] ivBytes;
        final byte[] dataToDecrypt;
    }

    public static CryptoModule effectiveCryptoModule(PubNub pubNub, String cipherKey) {
        return effectiveCryptoModule(pubNub.getCryptoModule(), cipherKey);
    }

    public static CryptoModule effectiveCryptoModule(CryptoModule cryptoModule, String cipherKey) {
        if (cipherKey != null) {
            return CryptoModule.createLegacyCryptoModule(cipherKey, true);
        } else {
            return cryptoModule;
        }
    }
}
