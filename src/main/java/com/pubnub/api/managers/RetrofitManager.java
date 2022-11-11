package com.pubnub.api.managers;

import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.callbacks.SubscribeCallback;
import com.pubnub.api.endpoints.vendor.AppEngineFactory;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.enums.PNStatusCategory;
import com.pubnub.api.interceptors.SignatureInterceptor;
import com.pubnub.api.models.consumer.PNStatus;
import com.pubnub.api.services.*;
import com.pubnub.api.services.vsp.SpaceService;
import com.pubnub.api.services.vsp.UserService;
import lombok.Getter;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import org.jetbrains.annotations.NotNull;
import retrofit2.Retrofit;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RetrofitManager {


    private PubNub pubnub;

    private SignatureInterceptor signatureInterceptor;

    private OkHttpClient transactionClientInstance;
    private OkHttpClient subscriptionClientInstance;
    private OkHttpClient noSignatureClientInstance;
    private OkHttpClient presenceClientInstance;


    // services
    @Getter
    private PresenceService presenceService;
    @Getter
    private HistoryService historyService;
    @Getter
    private PushService pushService;
    @Getter
    private AccessManagerService accessManagerService;
    @Getter
    private ChannelGroupService channelGroupService;
    @Getter
    private TimeService timeService;
    @Getter
    private PublishService publishService;
    @Getter
    private SubscribeService subscribeService;
    @Getter
    private SignalService signalService;
    @Getter
    private UUIDMetadataService uuidMetadataService;
    @Getter
    private UserService userService;
    @Getter
    private SpaceService spaceService;
    @Getter
    private ChannelMetadataService channelMetadataService;
    @Getter
    private MessageActionService messageActionService;
    @Getter
    private final FilesService filesService;
    @Getter
    private final S3Service s3Service;
    @Getter
    private final ExtendedPresenceService extendedPresenceService;
    @Getter
    private final Retrofit transactionInstance;
    @Getter
    private final Retrofit subscriptionInstance;
    @Getter
    private final Retrofit noSignatureInstance;
    @Getter
    private final Retrofit presenceInstance;

    public RetrofitManager(PubNub pubNubInstance) {
        this.pubnub = pubNubInstance;

        this.signatureInterceptor = new SignatureInterceptor(pubNubInstance);

        if (!pubNubInstance.getConfiguration().isGoogleAppEngineNetworking()) {
            this.transactionClientInstance = createOkHttpClient(
                    prepareOkHttpClient(
                            this.pubnub.getConfiguration().getNonSubscribeRequestTimeout(),
                            this.pubnub.getConfiguration().getConnectTimeout()
                    ).addInterceptor(this.signatureInterceptor)
                            .retryOnConnectionFailure(false)
            );

            Dispatcher dispatcher = new Dispatcher();
            dispatcher.setMaxRequestsPerHost(1);

            this.presenceClientInstance = createOkHttpClient(
                    prepareOkHttpClient(
                            this.pubnub.getConfiguration().getNonSubscribeRequestTimeout(),
                            this.pubnub.getConfiguration().getConnectTimeout()
                    ).addInterceptor(this.signatureInterceptor)
                            .retryOnConnectionFailure(false)
                            .dispatcher(dispatcher)
            );

            this.subscriptionClientInstance = createOkHttpClient(
                    prepareOkHttpClient(
                            this.pubnub.getConfiguration().getSubscribeTimeout(),
                            this.pubnub.getConfiguration().getConnectTimeout()
                    ).addInterceptor(this.signatureInterceptor)
                            .retryOnConnectionFailure(false)
            );

            this.noSignatureClientInstance = createOkHttpClient(
                    prepareOkHttpClient(this.pubnub.getConfiguration().getSubscribeTimeout(),
                            this.pubnub.getConfiguration().getConnectTimeout()
                    ).retryOnConnectionFailure(false)
            );

            //Because our users can think that PNStatusCategory.PNReconnectedCategory is about the whole
            //PubNub library and not only about the subscription loop just for safety we're going to
            //evict possibly broken connections for transactional calls
            this.pubnub.addListener(new SubscribeCallback.BaseSubscribeCallback() {
                @Override
                public void status(@NotNull final PubNub pubnub, @NotNull final PNStatus pnStatus) {
                    if (pnStatus.getCategory() == PNStatusCategory.PNReconnectedCategory) {
                        //On Android this callback is run on main thread therefore this thread is necessary
                        Executors.newSingleThreadExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                transactionClientInstance.connectionPool().evictAll();
                            }
                        });
                    }
                }
            });
        }

        this.transactionInstance = createRetrofit(this.transactionClientInstance);
        this.subscriptionInstance = createRetrofit(this.subscriptionClientInstance);
        this.noSignatureInstance = createRetrofit(this.noSignatureClientInstance);
        this.presenceInstance = createRetrofit(this.presenceClientInstance);

        this.presenceService = presenceInstance.create(PresenceService.class);
        this.historyService = transactionInstance.create(HistoryService.class);
        this.pushService = transactionInstance.create(PushService.class);
        this.accessManagerService = transactionInstance.create(AccessManagerService.class);
        this.channelGroupService = transactionInstance.create(ChannelGroupService.class);
        this.publishService = transactionInstance.create(PublishService.class);
        this.subscribeService = subscriptionInstance.create(SubscribeService.class);
        this.timeService = subscriptionInstance.create(TimeService.class);
        this.signalService = transactionInstance.create(SignalService.class);
        this.uuidMetadataService = transactionInstance.create(UUIDMetadataService.class);
        this.channelMetadataService = transactionInstance.create(ChannelMetadataService.class);
        this.messageActionService = transactionInstance.create(MessageActionService.class);
        this.filesService = transactionInstance.create(FilesService.class);
        this.s3Service = noSignatureInstance.create(S3Service.class);
        this.extendedPresenceService = transactionInstance.create(ExtendedPresenceService.class);

        this.userService = transactionInstance.create(UserService.class);
        this.spaceService = transactionInstance.create(SpaceService.class);
    }

    private OkHttpClient.Builder prepareOkHttpClient(int requestTimeout, int connectTimeOut) {
        PNConfiguration pnConfiguration = pubnub.getConfiguration();
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(requestTimeout, TimeUnit.SECONDS);
        httpClient.connectTimeout(connectTimeOut, TimeUnit.SECONDS);

        if (pubnub.getConfiguration().getLogVerbosity() == PNLogVerbosity.BODY) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(logging);
        }

        if (pubnub.getConfiguration().getHttpLoggingInterceptor() != null) {
            httpClient.addInterceptor(pubnub.getConfiguration().getHttpLoggingInterceptor());
        }

        if (pnConfiguration.getSslSocketFactory() != null && pnConfiguration.getX509ExtendedTrustManager() != null) {
            httpClient.sslSocketFactory(pnConfiguration.getSslSocketFactory(),
                    pnConfiguration.getX509ExtendedTrustManager());
        }

        if (pnConfiguration.getConnectionSpec() != null) {
            httpClient.connectionSpecs(Collections.singletonList(pnConfiguration.getConnectionSpec()));
        }

        if (pnConfiguration.getHostnameVerifier() != null) {
            httpClient.hostnameVerifier(pnConfiguration.getHostnameVerifier());
        }

        if (pubnub.getConfiguration().getProxy() != null) {
            httpClient.proxy(pubnub.getConfiguration().getProxy());
        }

        if (pubnub.getConfiguration().getProxySelector() != null) {
            httpClient.proxySelector(pubnub.getConfiguration().getProxySelector());
        }

        if (pubnub.getConfiguration().getProxyAuthenticator() != null) {
            httpClient.proxyAuthenticator(pubnub.getConfiguration().getProxyAuthenticator());
        }

        if (pubnub.getConfiguration().getCertificatePinner() != null) {
            httpClient.certificatePinner(pubnub.getConfiguration().getCertificatePinner());
        }


        return httpClient;
    }

    private OkHttpClient createOkHttpClient(OkHttpClient.Builder httpClient) {
        OkHttpClient constructedClient = httpClient.build();

        if (pubnub.getConfiguration().getMaximumConnections() != null) {
            constructedClient.dispatcher().setMaxRequestsPerHost(pubnub.getConfiguration().getMaximumConnections());
        }

        return constructedClient;
    }

    private Retrofit createRetrofit(OkHttpClient client) {
        return createRetrofit(client, pubnub.getBaseUrl());
    }

    private Retrofit createRetrofit(OkHttpClient client, String baseUrl) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();

        if (pubnub.getConfiguration().isGoogleAppEngineNetworking()) {
            retrofitBuilder.callFactory(new AppEngineFactory.Factory(pubnub));
        }

        retrofitBuilder = retrofitBuilder
                .baseUrl(baseUrl)
                .addConverterFactory(this.pubnub.getMapper().getConverterFactory());

        if (!pubnub.getConfiguration().isGoogleAppEngineNetworking()) {
            retrofitBuilder = retrofitBuilder.client(client);
        }

        return retrofitBuilder.build();
    }


    public ExecutorService getTransactionClientExecutorService() {
        return transactionClientInstance.dispatcher().executorService();
    }

    private void closeExecutor(OkHttpClient client, boolean force) {
        client.dispatcher().cancelAll();
        if (force) {
            client.connectionPool().evictAll();
            ExecutorService executorService = client.dispatcher().executorService();
            executorService.shutdown();
        }
    }

    public void destroy(boolean force) {
        if (this.transactionClientInstance != null) {
            closeExecutor(this.transactionClientInstance, force);
        }
        if (this.subscriptionClientInstance != null) {
            closeExecutor(this.subscriptionClientInstance, force);
        }
        if (this.noSignatureClientInstance != null) {
            closeExecutor(this.noSignatureClientInstance, force);
        }
    }
}
