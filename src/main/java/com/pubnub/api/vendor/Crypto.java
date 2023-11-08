package com.pubnub.api.vendor;

import com.pubnub.api.PubNubError;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Random;

import static com.pubnub.api.vendor.FileEncryptionUtil.CIPHER_TRANSFORMATION;
import static com.pubnub.api.vendor.FileEncryptionUtil.ENCODING_UTF_8;


@Slf4j
public class Crypto {

    byte[] keyBytes = null;
    byte[] ivBytes = null;
    String initializationVector = "0123456789012345";
    String cipherKey;
    boolean INIT = false;
    boolean dynamicIV = false;

    public Crypto(String cipherKey) {
        this(cipherKey, false);
    }

    public Crypto(String cipherKey, boolean dynamicIV) {
        this.cipherKey = cipherKey;
        this.dynamicIV = dynamicIV;
    }

    public Crypto(String cipherKey, String customInitializationVector) {
        if (customInitializationVector != null) {
            this.initializationVector = customInitializationVector;
        }

        this.cipherKey = cipherKey;
    }

    private void initCiphers() throws PubNubException {
        if (INIT && !dynamicIV)
            return;
        try {

            keyBytes = new String(hexEncode(sha256(this.cipherKey.getBytes(ENCODING_UTF_8))), ENCODING_UTF_8)
                    .substring(0, 32)
                    .toLowerCase().getBytes(ENCODING_UTF_8);
            if (dynamicIV){
                ivBytes = new byte[16];
                new Random().nextBytes(ivBytes);
            }
            else {
                ivBytes = initializationVector.getBytes(ENCODING_UTF_8);
                INIT = true;
            }
        } catch (UnsupportedEncodingException e) {
            throw PubNubException.builder().pubnubError(newCryptoError(11, e.toString())).errormsg(e.getMessage()).cause(e).build();
        }
    }

    public static byte[] hexEncode(byte[] input) throws PubNubException {
        StringBuffer result = new StringBuffer();
        for (byte byt : input)
            result.append(Integer.toString((byt & 0xff) + 0x100, 16).substring(1));
        try {
            return result.toString().getBytes(ENCODING_UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw PubNubException.builder().pubnubError(newCryptoError(12, e.toString())).errormsg(e.getMessage()).cause(e).build();
        }
    }

    private static PubNubError newCryptoError(int code, String message) {

        return PubNubErrorBuilder.createCryptoError(code, message);
    }

    public String encrypt(String input) throws PubNubException {
        try {
            initCiphers();
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = null;
            cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, newKey, ivSpec);
            if (dynamicIV) {
                byte[] encrypted = cipher.doFinal(input.getBytes(ENCODING_UTF_8));
                byte[] encryptedWithIV = new byte[ivBytes.length + encrypted.length];
                System.arraycopy(ivBytes, 0, encryptedWithIV, 0, ivBytes.length);
                System.arraycopy(encrypted, 0, encryptedWithIV, ivBytes.length, encrypted.length);
                return new String(Base64.encode(encryptedWithIV, 0), Charset.forName(ENCODING_UTF_8));
            }
            else {
                return new String(Base64.encode(cipher.doFinal(input.getBytes(ENCODING_UTF_8)), 0), Charset.forName(ENCODING_UTF_8));
            }
        } catch (NoSuchAlgorithmException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (NoSuchPaddingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (InvalidKeyException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (InvalidAlgorithmParameterException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (UnsupportedEncodingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (IllegalBlockSizeException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (BadPaddingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        }

    }

    /**
     * Decrypt
     *
     * @param cipher_text
     * @return String
     * @throws PubNubException
     */
    public String decrypt(String cipher_text) throws PubNubException {
        try {
            byte[] dataBytes = null;
            initCiphers();
            if (dynamicIV){
                dataBytes = Base64.decode(cipher_text, 0);
                System.arraycopy(dataBytes, 0, ivBytes, 0, 16);
                byte[] receivedCipherBytes = new byte[dataBytes.length - 16];
                System.arraycopy(dataBytes, 16, receivedCipherBytes, 0, dataBytes.length-16);
                dataBytes = receivedCipherBytes;
            }
            else {
                dataBytes = Base64.decode(cipher_text, 0);
            }
            AlgorithmParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
            Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, newKey, ivSpec);
            return new String(cipher.doFinal(dataBytes), ENCODING_UTF_8);
        } catch (IllegalArgumentException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (UnsupportedEncodingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (IllegalBlockSizeException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (BadPaddingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (InvalidKeyException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (InvalidAlgorithmParameterException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (NoSuchAlgorithmException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        } catch (NoSuchPaddingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
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
        } catch (NoSuchAlgorithmException e) {
            throw PubNubException.builder().pubnubError(newCryptoError(111, e.toString())).errormsg(e.getMessage()).cause(e).build();
        }
    }

}
