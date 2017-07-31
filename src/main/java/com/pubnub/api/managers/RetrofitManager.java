package com.pubnub.api.managers;


import com.pubnub.api.PNConfiguration;
import com.pubnub.api.PubNub;
import com.pubnub.api.endpoints.vendor.AppEngineFactory;
import com.pubnub.api.enums.PNLogVerbosity;
import com.pubnub.api.interceptors.SignatureInterceptor;

import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;

public class RetrofitManager {

    private PubNub pubnub;

    private SignatureInterceptor signatureInterceptor;

    private OkHttpClient transactionClientInstance;
    private OkHttpClient subscriptionClientInstance;

    @Getter private Retrofit transactionInstance;
    @Getter private Retrofit subscriptionInstance;

    public RetrofitManager(PubNub pubNubInstance) {
        this.pubnub = pubNubInstance;

        this.signatureInterceptor = new SignatureInterceptor(pubNubInstance);

        if (!pubNubInstance.getConfiguration().isGoogleAppEngineNetworking()) {
            this.transactionClientInstance = createOkHttpClient(
                    this.pubnub.getConfiguration().getNonSubscribeRequestTimeout(),
                    this.pubnub.getConfiguration().getConnectTimeout()
            );

            this.subscriptionClientInstance = createOkHttpClient(
                    this.pubnub.getConfiguration().getSubscribeTimeout(),
                    this.pubnub.getConfiguration().getConnectTimeout()
            );
        }

        this.transactionInstance = createRetrofit(this.transactionClientInstance);
        this.subscriptionInstance = createRetrofit(this.subscriptionClientInstance);
    }

    private OkHttpClient createOkHttpClient(int requestTimeout, int connectTimeOut) {
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
            httpClient.sslSocketFactory(pnConfiguration.getSslSocketFactory(), pnConfiguration.getX509ExtendedTrustManager());
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

        httpClient.addInterceptor(this.signatureInterceptor);

        return httpClient.build();
    }

    private Retrofit createRetrofit(OkHttpClient client) {
        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();

        if (pubnub.getConfiguration().isGoogleAppEngineNetworking()) {
            retrofitBuilder.callFactory(new AppEngineFactory.Factory(pubnub));
        }

        retrofitBuilder = retrofitBuilder
                .baseUrl(pubnub.getBaseUrl())
                .addConverterFactory(this.pubnub.getMapper().getConverterFactory());

        if (!pubnub.getConfiguration().isGoogleAppEngineNetworking()) {
             retrofitBuilder = retrofitBuilder
                     .client(client);
        }

        return retrofitBuilder.build();
    }

    public void destroy() {
        if (this.transactionClientInstance != null) {
            closeExecutor(this.transactionClientInstance);
        }
        if (this.subscriptionClientInstance != null) {
            closeExecutor(this.subscriptionClientInstance);
        }
    }

    private void closeExecutor(OkHttpClient client) {
        client.connectionPool().evictAll();
        client.dispatcher().cancelAll();
        ExecutorService executorService = client.dispatcher().executorService();
        executorService.shutdown();
    }
}
