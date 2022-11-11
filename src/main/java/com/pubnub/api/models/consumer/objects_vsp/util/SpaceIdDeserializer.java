package com.pubnub.api.models.consumer.objects_vsp.util;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.pubnub.api.SpaceId;
import lombok.SneakyThrows;

import java.lang.reflect.Type;

public class SpaceIdDeserializer implements JsonDeserializer<SpaceId> {
    @SneakyThrows
    @Override
    public SpaceId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        String spaceIdValue = json.getAsString();
        return new SpaceId(spaceIdValue);
    }
}
