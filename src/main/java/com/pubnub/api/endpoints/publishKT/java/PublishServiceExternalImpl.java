package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.endpoints.publishKT.PublishServiceExternal;
import com.pubnub.api.services.PublishService;
import org.jetbrains.annotations.NotNull;
import retrofit2.Call;

import java.util.List;
import java.util.Map;

public class PublishServiceExternalImpl implements PublishServiceExternal {
    private PublishService publishService;

    public PublishServiceExternalImpl(PublishService publishService) {
        this.publishService = publishService;
    }

    @NotNull
    @Override
    public Call<List<Object>> publish(@NotNull String pubKey, @NotNull String subKey, @NotNull String channel, @NotNull String message, @NotNull Map<String, String> options) {
        return publishService.publish(pubKey, subKey, channel, message, options);
    }

    @NotNull
    @Override
    public Call<List<Object>> publishWithPost(@NotNull String pubKey, @NotNull String subKey, @NotNull String channel, @NotNull Object message, @NotNull Map<String, String> options) {
        return publishService.publishWithPost(pubKey, subKey, channel, message, options);
    }
}
