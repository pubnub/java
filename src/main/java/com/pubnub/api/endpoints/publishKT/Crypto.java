package com.pubnub.api.endpoints.publishKT;


import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import static com.pubnub.api.endpoints.publishKT.FileEncryptionUtil.CIPHER_TRANSFORMATION;
import static com.pubnub.api.endpoints.publishKT.FileEncryptionUtil.ENCODING_UTF_8;


public class Crypto {

    private final boolean dynamicIV;
    byte[] keyBytes = null;
    byte[] ivBytes = null;
    String initializationVector = "0123456789012345";
    String cipherKey;
    boolean INIT = false;

    public Crypto(String cipherKey) {
        this(cipherKey, false);
    }

    public Crypto(String cipherKey, String customInitializationVector) {
        this(cipherKey, false);
        if (customInitializationVector != null) {
            this.initializationVector = customInitializationVector;
        }
    }

    public Crypto(String cipherKey, boolean dynamicIV) {
        this.cipherKey = cipherKey;
        this.dynamicIV = dynamicIV;
    }

    public void initCiphers() throws PubNubException {
        if (INIT && !dynamicIV)
            return;
        try {

            keyBytes = new String(hexEncode(sha256(this.cipherKey.getBytes(ENCODING_UTF_8))), ENCODING_UTF_8)
                    .substring(0, 32)
                    .toLowerCase().getBytes(ENCODING_UTF_8);
            if (dynamicIV) {
                ivBytes = new byte[16];
                new Random().nextBytes(ivBytes);
            } else {
                ivBytes = initializationVector.getBytes(ENCODING_UTF_8);
                INIT = true;
            }
        } catch (UnsupportedEncodingException e) {
            throw newCryptoError(11, e);
        }
    }

    public static byte[] hexEncode(byte[] input) throws PubNubException {
        StringBuffer result = new StringBuffer();
        for (byte byt : input)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        try {
            return result.toString().getBytes(ENCODING_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw newCryptoError(12, e);
        }
    }

    public static PubNubException newCryptoError(int code, Exception exception) {
        return new PubNubException(
                exception.getClass().getSimpleName() + " " + exception.getMessage() + " " + code,
                PubNubError.CRYPTO_ERROR,
                null,
                0,
                null
        );
    }

    public String encrypt(String input) throws PubNubException {
        return encrypt(input, Base64.NO_WRAP);
    }

    public String encrypt(String input, Integer flags) throws PubNubException {
        try {
            initCiphers();
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            if (dynamicIV) {
                byte[] encrypted = cipher.doFinal(input.getBytes(ENCODING_UTF_8));
                byte[] encryptedWithIV = new byte[ivBytes.length + encrypted.length];
                System.arraycopy(ivBytes, 0, encryptedWithIV, 0, ivBytes.length);
                System.arraycopy(encrypted, 0, encryptedWithIV, ivBytes.length, encrypted.length);
                return new String(Base64.encode(encryptedWithIV, flags), ENCODING_UTF_8);
            } else {
                return new String(Base64.encode(cipher.doFinal(input.getBytes(ENCODING_UTF_8)), flags), ENCODING_UTF_8);
            }
        } catch (Exception e) {
            throw newCryptoError(0, e);
        }

    }

    public String decrypt(String cipher_text) throws PubNubException {
        return decrypt(cipher_text, Base64.NO_WRAP);
    }

    public String decrypt(String cipher_text, Integer flags) throws PubNubException {
        try {
            byte[] dataBytes;
            initCiphers();
            if (dynamicIV) {
                dataBytes = Base64.decode(cipher_text, flags);
                System.arraycopy(dataBytes, 0, ivBytes, 0, 16);
                byte[] receivedCipherBytes = new byte[dataBytes.length - 16];
                System.arraycopy(dataBytes, 16, receivedCipherBytes, 0, dataBytes.length - 16);
                dataBytes = receivedCipherBytes;
            } else {
                dataBytes = Base64.decode(cipher_text, flags);
            }
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return new String(cipher.doFinal(dataBytes), ENCODING_UTF_8);
        } catch (Exception e) {
            throw newCryptoError(0, e);
        }
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Get MD5
     *
     * @param input
     * @return byte[]
     * @throws PubNubException
     */
    public static byte[] md5(String input) throws PubNubException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            byte[] hashedBytes = digest.digest(input.getBytes(ENCODING_UTF_8));
            return hashedBytes;
        } catch (Exception e) {
            throw newCryptoError(0, e);
        }
    }

    /**
     * Get SHA256
     *
     * @param input
     * @return byte[]
     * @throws PubNubException
     */
    public static byte[] sha256(byte[] input) throws PubNubException {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(input);
            return hashedBytes;
        } catch (Exception e) {
            throw newCryptoError(0, e);
        }
    }

}
