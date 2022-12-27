package com.pubnub.api.endpoints.access.builder;

import com.pubnub.api.PubNubException;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.access.GrantToken;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.models.consumer.access_manager.v3.PNGrantTokenResult;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public abstract class AbstractGrantTokenBuilder<T> implements RemoteAction<PNGrantTokenResult> {
    protected final GrantToken grantToken;

    public AbstractGrantTokenBuilder(GrantToken grantToken) {
        this.grantToken = grantToken;
    }

    @Override
    public PNGrantTokenResult sync() throws PubNubException {
        return grantToken.sync();
    }

    @Override
    public void async(@NotNull PNCallback<PNGrantTokenResult> callback) {
        grantToken.async(callback);
    }

    @Override
    public void retry() {
        grantToken.retry();
    }

    @Override
    public void silentCancel() {
        grantToken.silentCancel();
    }

    public abstract T queryParam(Map<String, String> queryParam);
}
