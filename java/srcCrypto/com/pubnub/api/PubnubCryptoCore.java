package com.pubnub.api;

import org.bouncycastle.crypto.DataLengthException;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.CBCBlockCipher;
import org.bouncycastle.crypto.paddings.PaddedBufferedBlockCipher;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.crypto.params.ParametersWithIV;
import org.bouncycastle.util.encoders.Hex;

import java.io.*;

/**
 * PubNub 3.1 Cryptography
 *
 */
abstract class PubnubCryptoCore {

    PaddedBufferedBlockCipher encryptCipher = null;
    PaddedBufferedBlockCipher decryptCipher = null;
    byte[] buf = new byte[16]; // input buffer
    byte[] obuf = new byte[512]; // output buffer
    byte[] key = null;
    String IV = "0123456789012345";
    public static int blockSize = 16;
    String CIPHER_KEY;

    public PubnubCryptoCore(String CIPHER_KEY) {
        this.CIPHER_KEY = CIPHER_KEY;
    }
    public PubnubCryptoCore(String CIPHER_KEY, String initialization_vector) {
        if (initialization_vector != null) this.IV = initialization_vector;
        this.CIPHER_KEY = CIPHER_KEY;
    }

    private static PubnubError newCryptoError(int code, String message) {
        return PubnubError.getErrorObject(PubnubError.PNERROBJ_CRYPTO_ERROR, code, message);
    }


    public void InitCiphers() throws PubnubException {

        try {

            key = new String(Hex.encode(sha256(this.CIPHER_KEY.getBytes("UTF-8"))),
                             "UTF-8").substring(0, 32).toLowerCase().getBytes("UTF-8");
            encryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(
                    new AESEngine()));

            decryptCipher = new PaddedBufferedBlockCipher(new CBCBlockCipher(
                        new AESEngine()));

            // create the IV parameter
            ParametersWithIV parameterIV = new ParametersWithIV(new KeyParameter(
                        key), IV.getBytes("UTF-8"));

            encryptCipher.init(true, parameterIV);
            decryptCipher.init(false, parameterIV);
        } catch (UnsupportedEncodingException e) {
            throw new PubnubException(newCryptoError(1, e.toString()));
        }

    }

    public void ResetCiphers() {
        if (encryptCipher != null) {
            encryptCipher.reset();
        }
        if (decryptCipher != null) {
            decryptCipher.reset();
        }
    }

    public String encrypt(String input) throws PubnubException {
        try {
            InputStream st = new ByteArrayInputStream(input.getBytes("UTF-8"));
            ByteArrayOutputStream ou = new ByteArrayOutputStream();
            CBCEncryptOrDecrypt(st, ou, true);
            String s = new String(Base64Encoder.encode(ou.toByteArray()));
            return s;
        } catch (IOException e) {
            throw new PubnubException(newCryptoError(2, e.toString()));
        }
    }

    /**
     * Decrypt
     *
     * @param cipher_text
     * @return String
     * @throws PubnubException
     */
    public String decrypt(String cipher_text) throws PubnubException {
        try {
            byte[] cipher = Base64Encoder.decode(cipher_text);
            InputStream st = new ByteArrayInputStream(cipher);
            ByteArrayOutputStream ou = new ByteArrayOutputStream();
            CBCEncryptOrDecrypt(st, ou, false);
            return new String(ou.toByteArray());
        } catch (IllegalArgumentException e) {
            throw new PubnubException(newCryptoError(3, e.toString()));
        }
    }

    public void CBCEncryptOrDecrypt(InputStream in, OutputStream out,
                                    boolean encrypt) throws PubnubException {
        if (encryptCipher == null || decryptCipher == null) {
            InitCiphers();
        }
        PaddedBufferedBlockCipher cipher = (encrypt) ? encryptCipher
                                           : decryptCipher;
        int noBytesRead = 0; // number of bytes read from input
        int noBytesProcessed = 0; // number of bytes processed

        try {
            while ((noBytesRead = in.read(buf)) >= 0) {
                noBytesProcessed = cipher
                                   .processBytes(buf, 0, noBytesRead, obuf, 0);
                out.write(obuf, 0, noBytesProcessed);
            }
            noBytesProcessed = cipher.doFinal(obuf, 0);
            out.write(obuf, 0, noBytesProcessed);
            out.flush();
            in.close();
            out.close();
        } catch (DataLengthException e) {
            throw new PubnubException(newCryptoError(4, e.toString()));
        } catch (IllegalStateException e) {
            throw new PubnubException(newCryptoError(5, e.toString()));
        } catch (IOException e) {
            throw new PubnubException(newCryptoError(6, e.toString()));
        } catch (InvalidCipherTextException e) {
            throw new PubnubException(newCryptoError(7, e.toString()));
        }

    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                                  .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Get MD5
     *
     * @param input
     * @return byte[]
     */
    public static byte[] md5(String input) {
        MD5Digest digest = new MD5Digest();
        byte[] bytes = input.getBytes();
        digest.update(bytes, 0, bytes.length);
        byte[] md5 = new byte[digest.getDigestSize()];
        digest.doFinal(md5, 0);
        StringBuffer hex = new StringBuffer(md5.length * 2);
        for (int i = 0; i < md5.length; i++) {
            byte b = md5[i];
            if ((b & 0xFF) < 0x10) {
                hex.append("0");
            }
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hexStringToByteArray(hex.toString());
    }

    /**
     * Get SHA256
     *
     * @param input
     * @return byte[]
     */
    public static byte[] sha256(byte[] input) {

        Digest digest = new SHA256Digest();
        byte[] resBuf = new byte[digest.getDigestSize()];
        byte[] bytes = input;
        digest.update(bytes, 0, bytes.length);
        digest.doFinal(resBuf, 0);
        return resBuf;
    }

    public static byte[] hexEncode(byte[] input) throws PubnubException {
        return Hex.encode(input);
    }

}
