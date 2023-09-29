package com.pubnub.api.vendor;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.crypto.CryptoModule;
import lombok.Data;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import static com.pubnub.api.PubNubUtil.readBytes;
import static com.pubnub.api.vendor.Crypto.hexEncode;
import static com.pubnub.api.vendor.Crypto.sha256;

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

    public static byte[] encryptToBytes(final String cipherKey, final byte[] bytesToEncrypt)
            throws PubNubException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            final byte[] keyBytes = keyBytes(cipherKey);
            final byte[] randomIvBytes = randomIv();
            final Cipher encryptionCipher = encryptionCipher(keyBytes, randomIvBytes);

            byteArrayOutputStream.write(randomIvBytes);
            byteArrayOutputStream.write(encryptionCipher.doFinal(bytesToEncrypt));
            return byteArrayOutputStream.toByteArray();
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException |
                InvalidKeyException | IOException | BadPaddingException | IllegalBlockSizeException e) {
            throw PubNubException.builder().errormsg(e.toString()).build();
        }
    }

    public static InputStream encrypt(final String cipherKey, final InputStream inputStreamToEncrypt)
            throws PubNubException {

        try {
            return new ByteArrayInputStream(encryptToBytes(cipherKey, readBytes(inputStreamToEncrypt)));
        } catch (IOException e) {
            throw PubNubException.builder()
                    .errormsg(e.getMessage())
                    .cause(e)
                    .build();
        }
    }

    public static InputStream decrypt(final String cipherKey, final InputStream encryptedInputStream)
            throws PubNubException {
        try {
            final byte[] keyBytes = keyBytes(cipherKey);
            final IvAndData ivAndData = loadIvAndDataFromInputStream(encryptedInputStream);
            final Cipher decryptionCipher = decryptionCipher(keyBytes, ivAndData.ivBytes);
            byte[] decryptedBytes = decryptionCipher.doFinal(ivAndData.dataToDecrypt);
            return new ByteArrayInputStream(decryptedBytes);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException
                | InvalidKeyException | IOException | IllegalBlockSizeException | BadPaddingException e) {
            throw PubNubException.builder().errormsg(e.toString()).cause(e).build();
        }
    }

    private static IvAndData loadIvAndDataFromInputStream(final InputStream inputStreamToEncrypt) throws IOException {
        final byte[] ivBytes = new byte[IV_SIZE_BYTES];
        {
            int read;
            int readSoFar = 0;
            do {
                read = inputStreamToEncrypt.read(ivBytes, readSoFar, IV_SIZE_BYTES - readSoFar);
                if (read != -1) {
                    readSoFar += read;
                }
            } while (read != -1 && readSoFar < IV_SIZE_BYTES);
            if (read == -1) {
                throw new IOException("EOF before IV fully read");
            }
        }

        return new IvAndData(ivBytes, readBytes(inputStreamToEncrypt));
    }

    private static Cipher encryptionCipher(final byte[] keyBytes, final byte[] ivBytes)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        return cipher(keyBytes, ivBytes, Cipher.ENCRYPT_MODE);
    }

    private static Cipher decryptionCipher(final byte[] keyBytes, final byte[] ivBytes)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        return cipher(keyBytes, ivBytes, Cipher.DECRYPT_MODE);
    }

    private static Cipher cipher(final byte[] keyBytes, final byte[] ivBytes, final int mode)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException {
        Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
        AlgorithmParameterSpec iv = new IvParameterSpec(ivBytes);
        SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
        cipher.init(mode, key, iv);
        return cipher;
    }

    private static byte[] keyBytes(final String cipherKey) throws UnsupportedEncodingException, PubNubException {
        return new String(hexEncode(sha256(cipherKey.getBytes(ENCODING_UTF_8))), ENCODING_UTF_8)
                .substring(0, 32)
                .toLowerCase().getBytes(ENCODING_UTF_8);
    }

    private static byte[] randomIv() throws NoSuchAlgorithmException {
        byte[] randomIv = new byte[IV_SIZE_BYTES];
        SecureRandom.getInstance("SHA1PRNG").nextBytes(randomIv);
        return randomIv;
    }
}
