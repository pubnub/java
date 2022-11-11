package com.pubnub.api.models.consumer.objects_vsp.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pubnub.api.UserId;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public class UserIdDeserializer implements JsonDeserializer<UserId> {
    @SneakyThrows
    @Override
    public UserId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String userIdValue = json.getAsString();
        return new UserId(userIdValue);
    }
}
