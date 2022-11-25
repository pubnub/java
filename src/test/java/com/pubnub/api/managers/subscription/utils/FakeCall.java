package com.pubnub.api.managers.subscription.utils;

import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
public class FakeCall<T> implements Call<T> {
    private final ResponseSupplier<T> responseSupplier;
    private final Request request;
    private final ExecutorService executor;
    private boolean executed = false;

    public FakeCall(final Request request, final ResponseSupplier<T> responseSupplier) {
        this.request = request;
        this.responseSupplier = responseSupplier;
        this.executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public Response<T> execute() throws IOException {
        if (executed) throw new IllegalStateException("Already executed.");
        executed = true;

        log.info("executing the call with request: " + request);
        final RequestDetails requestDetails = request.tag(RequestDetails.class);
        ResponseHolder<T> responseHolder = responseSupplier.get(requestDetails);
        final Exception exception = responseHolder.getException();
        if (exception == null) {
            return responseHolder.getResponse();
        } else {
            if (exception instanceof IOException) {
                throw (IOException) exception;
            } else {
                throw new RuntimeException(exception);
            }
        }
    }

    @Override
    public void enqueue(final Callback<T> callback) {
        if (executed) throw new IllegalStateException("Already executed.");
        executed = true;

        executor.execute(new Runnable() {
            @Override
            public void run() {
//                log.info("asynchronously executing the call with request: " + request);
                final RequestDetails requestDetails = request.tag(RequestDetails.class);
                final ResponseHolder<T> responseHolder = responseSupplier.get(requestDetails);
                final Exception exception = responseHolder.getException();
                if (exception == null) {
                    callback.onResponse(FakeCall.this, responseHolder.getResponse());
                } else {
                    callback.onFailure(FakeCall.this, exception);
                }
            }
        });
    }

    @Override
    public boolean isExecuted() {
        return executed;
    }

    @Override
    public void cancel() {
    }

    @Override
    public boolean isCanceled() {
        return false;
    }

    @Override
    public Call<T> clone() {
        return new FakeCall<T>(request, responseSupplier);
    }

    @Override
    public Request request() {
        return request;
    }
}
