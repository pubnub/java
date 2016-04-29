package com.pubnub.api.callbacks;


import com.pubnub.api.models.consumer.PNStatus;

public abstract class PNCallback<X> {
    public abstract void onResponse(X result, PNStatus status);
}

