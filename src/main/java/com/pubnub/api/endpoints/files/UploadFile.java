package com.pubnub.api.endpoints.files;

import com.pubnub.api.PubNub;
import com.pubnub.api.PubNubException;
import com.pubnub.api.builder.PubNubErrorBuilder;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.enums.PNOperationType;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.managers.RetrofitManager;
import com.pubnub.api.models.consumer.PNErrorData;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.models.server.files.FileUploadRequestDetails;
import com.pubnub.api.models.server.files.FormField;
import com.pubnub.api.services.S3Service;
import com.pubnub.api.vendor.FileEncryptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import retrofit2.Call;
import retrofit2.Response;

import javax.net.ssl.SSLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.List;

import static com.pubnub.api.vendor.FileEncryptionUtil.effectiveCipherKey;

@Slf4j
class UploadFile implements RemoteAction<Void> {
    private static final MediaType APPLICATION_OCTET_STREAM = MediaType.get("application/octet-stream");
    private static final String CONTENT_TYPE_HEADER = "Content-Type";
    private static final String FILE_PART_MULTIPART = "file";
    private final S3Service s3Service;
    private final String fileName;
    private final byte[] content;
    private final String cipherKey;
    private final FormField key;
    private final List<FormField> formParams;
    private final String baseUrl;
    private Call<Void> call;

    UploadFile(S3Service s3Service,
               String fileName,
               byte[] content,
               String cipherKey,
               FormField key,
               List<FormField> formParams,
               String baseUrl) {
        this.s3Service = s3Service;
        this.fileName = fileName;
        this.content = content;
        this.cipherKey = cipherKey;
        this.key = key;
        this.formParams = formParams;
        this.baseUrl = baseUrl;
    }

    private static void addFormParamsWithKeyFirst(FormField keyValue,
                                                  List<FormField> formParams,
                                                  MultipartBody.Builder builder) {
        builder.addFormDataPart(keyValue.getKey(), keyValue.getValue());
        for (FormField it : formParams) {
            if (!it.getKey().equals(keyValue.getKey())) {
                builder.addFormDataPart(it.getKey(), it.getValue());
            }
        }
    }

    private Call<Void> prepareCall() throws PubNubException, IOException {
        MultipartBody.Builder builder = new MultipartBody.Builder().setType(MultipartBody.FORM);
        addFormParamsWithKeyFirst(key, formParams, builder);
        MediaType mediaType = getMediaType(getContentType(formParams));

        RequestBody requestBody;
        if (cipherKey == null) {
            requestBody = RequestBody.create(content, mediaType);
        } else {
            requestBody = RequestBody.create(FileEncryptionUtil.encryptToBytes(cipherKey, content), mediaType);
        }

        builder.addFormDataPart(FILE_PART_MULTIPART, fileName, requestBody);
        return s3Service.upload(baseUrl, builder.build());
    }

    @Nullable
    private String getContentType(List<FormField> formFields) {
        String contentType = null;
        for (FormField field : formFields) {
            if (field.getKey().equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
                contentType = field.getValue();
                break;
            }
        }
        return contentType;
    }

    private MediaType getMediaType(@Nullable String contentType) {
        if (contentType == null) {
            return APPLICATION_OCTET_STREAM;
        }

        try {
            return MediaType.get(contentType);
        } catch (Throwable t) {
            log.warn("Content-Type: " + contentType + " was not recognized by MediaType.get", t);
            return APPLICATION_OCTET_STREAM;
        }
    }

    @Override
    public Void sync() throws PubNubException {
        try {
            call = prepareCall();
        } catch (IOException e) {
            throw PubNubException.builder()
                    .errormsg(e.getMessage())
                    .cause(e)
                    .build();
        }

        Response<Void> serverResponse;
        try {
            serverResponse = call.execute();
        } catch (IOException e) {
            throw PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_PARSING_ERROR)
                    .errormsg(e.toString())
                    .affectedCall(call)
                    .cause(e)
                    .build();
        }

        if (!serverResponse.isSuccessful()) {
            throw createException(serverResponse);
        }
        return null;
    }

    @Override
    public void async(@NotNull PNCallback<Void> callback) {
        try {
            call = prepareCall();
            call.enqueue(new retrofit2.Callback<Void>() {

                @Override
                public void onResponse(@NotNull Call<Void> performedCall, @NotNull Response<Void> response) {
                    if (!response.isSuccessful()) {
                        PubNubException ex = createException(response);

                        PNStatusCategory pnStatusCategory = PNStatusCategory.PNUnknownCategory;

                        if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED
                                || response.code() == HttpURLConnection.HTTP_FORBIDDEN) {
                            pnStatusCategory = PNStatusCategory.PNAccessDeniedCategory;
                        }

                        if (response.code() == HttpURLConnection.HTTP_BAD_REQUEST) {
                            pnStatusCategory = PNStatusCategory.PNBadRequestCategory;
                        }

                        callback.onResponse(null,
                                createStatusResponse(pnStatusCategory, response, ex));
                        return;
                    }

                    callback.onResponse(null,
                            createStatusResponse(PNStatusCategory.PNAcknowledgmentCategory, response,
                                    null));
                }

                @Override
                public void onFailure(@NotNull Call<Void> performedCall, @NotNull Throwable throwable) {
                    if (call.isCanceled()) {
                        return;
                    }

                    PNStatusCategory pnStatusCategory;

                    PubNubException.PubNubExceptionBuilder pubnubException = PubNubException.builder()
                            .errormsg(throwable.getMessage())
                            .cause(throwable);

                    try {
                        throw throwable;
                    } catch (UnknownHostException networkException) {
                        pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_CONNECTION_NOT_SET);
                        pnStatusCategory = PNStatusCategory.PNUnexpectedDisconnectCategory;
                    } catch (SocketException | SSLException exception) {
                        pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_CONNECT_EXCEPTION);
                        pnStatusCategory = PNStatusCategory.PNUnexpectedDisconnectCategory;
                    } catch (SocketTimeoutException socketTimeoutException) {
                        pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_SOCKET_TIMEOUT);
                        pnStatusCategory = PNStatusCategory.PNTimeoutCategory;
                    } catch (Throwable throwable1) {
                        pubnubException.pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR);
                        if (performedCall.isCanceled()) {
                            pnStatusCategory = PNStatusCategory.PNCancelledCategory;
                        } else {
                            pnStatusCategory = PNStatusCategory.PNBadRequestCategory;
                        }
                    }

                    callback.onResponse(null, createStatusResponse(pnStatusCategory, null, pubnubException.build()));

                }
            });

        } catch (IOException | PubNubException e) {
            //FIXME which category shall this error belong to?
            callback.onResponse(null,
                    createStatusResponse(PNStatusCategory.PNUnknownCategory, null, e));
        }
    }

    @Override
    public void retry() {
    }

    @Override
    public void silentCancel() {
        if (!call.isCanceled()) {
            call.cancel();
        }
    }

    private PubNubException createException(Response<Void> response) {
        try {
            String responseBodyText = "N/A";
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(response.errorBody().byteStream());
            doc.getDocumentElement().normalize();
            NodeList elements = doc.getElementsByTagName("Message");
            for (int i = 0; i < elements.getLength(); i++) {
                responseBodyText = elements.item(0).getFirstChild().getNodeValue();
            }

            return PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR)
                    .errormsg(responseBodyText)
                    .affectedCall(call)
                    .statusCode(response.code())
                    .build();
        } catch (IOException | ParserConfigurationException | SAXException | NullPointerException e) {
            return PubNubException.builder()
                    .pubnubError(PubNubErrorBuilder.PNERROBJ_HTTP_ERROR)
                    .errormsg(e.getMessage())
                    .affectedCall(call)
                    .statusCode(response.code())
                    .cause(e)
                    .build();
        }
    }

    private PNStatus createStatusResponse(PNStatusCategory category, Response<Void> response, Exception throwable) {
        PNStatus.PNStatusBuilder pnStatus = PNStatus.builder();

        if (response == null || throwable != null) {
            pnStatus.error(true);
        }
        if (throwable != null) {
            PNErrorData pnErrorData = new PNErrorData(throwable.getMessage(), throwable);
            pnStatus.errorData(pnErrorData);
        }

        if (response != null) {
            pnStatus.statusCode(response.code());
            pnStatus.tlsEnabled(response.raw().request().url().isHttps());
            pnStatus.origin(response.raw().request().url().host());
            pnStatus.clientRequest(response.raw().request());
        }

        pnStatus.operation(getOperationType());
        pnStatus.category(category);

        return pnStatus.build();
    }

    private PNOperationType getOperationType() {
        return PNOperationType.PNFileAction;
    }

    static class Factory {
        private final PubNub pubNub;
        private final RetrofitManager retrofitManager;

        Factory(PubNub pubNub, RetrofitManager retrofitManager) {
            this.pubNub = pubNub;
            this.retrofitManager = retrofitManager;
        }

        RemoteAction<Void> create(String fileName,
                                  byte[] content,
                                  String cipherKey,
                                  FileUploadRequestDetails fileUploadRequestDetails) {
            String effectiveCipherKey = effectiveCipherKey(pubNub, cipherKey);

            return new UploadFile(retrofitManager.getS3Service(),
                    fileName,
                    content,
                    effectiveCipherKey,
                    fileUploadRequestDetails.getKeyFormField(), fileUploadRequestDetails.getFormFields(),
                    fileUploadRequestDetails.getUrl());
        }
    }

}
