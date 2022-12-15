package com.pubnub.api.vendor;

import com.pubnub.api.PubNubException;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Random;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

public class CryptoTest {
    private static final int MAX_FILE_SIZE_IN_BYTES = 1024 * 1024 * 5;

    @Test
    public void canDecryptWhatIsEncrypted() throws IOException, PubNubException {
        //given
        final String cipherKey = "enigma";
        final byte[] byteArrayToEncrypt = byteArrayToEncrypt();
        byte[] decryptedByteArray;

        //when
        final byte[] encryptedByteArray = FileEncryptionUtil.encryptToBytes(cipherKey,
                byteArrayToEncrypt);
        try (InputStream decryptedInputStream = FileEncryptionUtil.decrypt(cipherKey,
                new ByteArrayInputStream(encryptedByteArray))) {
            try (final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
                IOUtils.copy(decryptedInputStream, byteArrayOutputStream);
                decryptedByteArray = byteArrayOutputStream.toByteArray();
            }
        }

        //then
        assertThat(decryptedByteArray, allOf(
                equalTo(byteArrayToEncrypt),
                not(equalTo(encryptedByteArray))));
    }

    private byte[] byteArrayToEncrypt() {
        final Random random = new Random();
        final int fileSize = random.nextInt(MAX_FILE_SIZE_IN_BYTES);
        byte[] fileContents = new byte[fileSize];
        random.nextBytes(fileContents);
        return fileContents;
    }
}