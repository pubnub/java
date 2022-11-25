package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNubException;
import com.pubnub.api.models.server.files.FormField;
import com.pubnub.api.services.S3Service;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.ArgumentCaptor;
import org.mockito.stubbing.Answer;
import retrofit2.Call;
import retrofit2.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;
import java.util.function.Supplier;

import static com.pubnub.api.PubNubUtil.readBytes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class UploadFileTest implements TestsWithFiles {
    private final S3Service s3Service = mock(S3Service.class);
    private final ArgumentCaptor<MultipartBody> requestBodyArgumentCaptor = ArgumentCaptor.forClass(MultipartBody.class);

    @SuppressWarnings("unchecked")
    @NotNull
    protected static <T> Answer<Call<T>> mockRetrofitSuccessfulCall(final Supplier<T> block) {
        return invocation -> {
            final Call<T> mockCall = mock(Call.class);
            when(mockCall.execute()).thenAnswer(blockInvocation -> Response.success(block.get()));
            return mockCall;
        };
    }

    @SuppressWarnings("unchecked")
    @NotNull
    protected static <T extends String> Answer<Call<T>> mockRetrofitErrorCall(final Supplier<T> block) {
        return invocation -> {
            final Call<T> mockCall = mock(Call.class);
            when(mockCall.execute()).thenAnswer(blockInvocation -> Response.error(400, ResponseBody.create(block.get(), MediaType.get("application/xml"))));
            return mockCall;
        };
    }

    @Test
    public void keyIsFirstInMultipart() throws PubNubException, IOException {
        //given
        File file = getTemporaryFile("file.txt");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            UploadFile uploadFile = new UploadFile(s3Service,
                    file.getName(),
                    readBytes(fileInputStream),
                    null,

                    new FormField("key", "keyValue"),
                    Collections.singletonList(new FormField("other", "otherValue")),
                    "https://s3.aws.com/bucket"
            );

            when(s3Service.upload(any(), any())).then(mockRetrofitSuccessfulCall(() -> null));

            //when
            uploadFile.sync();

            //then
            verify(s3Service, times(1)).upload(any(), requestBodyArgumentCaptor.capture());
        }

        MultipartBody capturedBody = requestBodyArgumentCaptor.getValue();

        assertEquals("form-data; name=\"key\"", capturedBody.part(0).headers().get("Content-Disposition"));
        assertPartExist("other", capturedBody.parts());
        assertPartExist("file", capturedBody.parts());

    }

    @Test
    public void contentTypeIsUsedForFileIfPresentInFormFields() throws PubNubException, IOException {
        //given
        File file = getTemporaryFile("file.txt");
        String contentTypeValue = "application/json";
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            UploadFile uploadFile = new UploadFile(s3Service,
                    file.getName(),
                    readBytes(fileInputStream),
                    null,
                    new FormField("key", "keyValue"),
                    Collections.singletonList(new FormField("Content-Type", contentTypeValue)),
                    "https://s3.aws.com/bucket"
            );

            when(s3Service.upload(any(), any())).then(mockRetrofitSuccessfulCall(() -> null));

            //when
            uploadFile.sync();

            //then
            verify(s3Service, times(1)).upload(any(), requestBodyArgumentCaptor.capture());
        }

        MultipartBody capturedBody = requestBodyArgumentCaptor.getValue();

        assertPartExist("file", capturedBody.parts());
        MultipartBody.Part filePart = getPart("file", capturedBody.parts());
        assertEquals(MediaType.get(contentTypeValue), filePart.body().contentType());
    }

    @Test
    public void defaultContentTypeIsUsedForFileIfNotPresentInFormFields() throws PubNubException, IOException {
        //given
        File file = getTemporaryFile("file.txt");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            UploadFile uploadFile = new UploadFile(s3Service,
                    file.getName(),
                    readBytes(fileInputStream),
                    null,
                    new FormField("key", "keyValue"),
                    Collections.emptyList(),
                    "https://s3.aws.com/bucket"
            );

            when(s3Service.upload(any(), any())).then(mockRetrofitSuccessfulCall(() -> null));

            //when
            uploadFile.sync();

            //then
            verify(s3Service, times(1)).upload(any(), requestBodyArgumentCaptor.capture());
        }

        MultipartBody capturedBody = requestBodyArgumentCaptor.getValue();

        assertPartExist("file", capturedBody.parts());
        MultipartBody.Part filePart = getPart("file", capturedBody.parts());
        assertEquals(MediaType.get("application/octet-stream"), filePart.body().contentType());
    }

    @Test
    public void errorMessageIsCopiedFromS3XMLResponse() throws IOException{
        //given
        File file = getTemporaryFile("file.txt");
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            UploadFile uploadFile = new UploadFile(s3Service,
                    file.getName(),
                    readBytes(fileInputStream),
                    null,
                    new FormField("key", "keyValue"),
                    Collections.emptyList(),
                    "https://s3.aws.com/bucket"
            );

            when(s3Service.upload(any(), any())).then(mockRetrofitErrorCall(() -> readToString(UploadFileTest.class.getResourceAsStream("/entityTooLarge.xml"))));

            //when
            try {
                uploadFile.sync();
                Assert.fail("Exception expected");
            } catch (PubNubException ex) {
                //then
                Assert.assertEquals("Your proposed upload exceeds the maximum allowed size", ex.getErrormsg());
            }
        }
    }

    private String readToString(InputStream inputStream) {
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }


    private MultipartBody.Part getPart(String partName, List<MultipartBody.Part> parts) {
        MultipartBody.Part result = null;
        for (MultipartBody.Part part : parts) {
            if (part.headers().get("Content-Disposition").contains(partName)) {
                result = part;
                break;
            }
        }
        return result;
    }

    private void assertPartExist(String partName, List<MultipartBody.Part> parts) {
        MultipartBody.Part result = getPart(partName, parts);
        if (result == null) {
            fail("There's no part " + partName + " in parts " + parts);
        }
    }

    @Override
    @Rule
    public TemporaryFolder getTemporaryFolder() {
        return folder;
    }
}
