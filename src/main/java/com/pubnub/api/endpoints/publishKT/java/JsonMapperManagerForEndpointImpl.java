package com.pubnub.api.endpoints.publishKT.java;

import com.google.gson.JsonElement;
import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.publishKT.JsonMapperManagerForEndpoint;
import com.pubnub.api.managers.MapperManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class JsonMapperManagerForEndpointImpl implements JsonMapperManagerForEndpoint {
    private final MapperManager mapperManager;

    public JsonMapperManagerForEndpointImpl(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    @Override
    public boolean isJsonObject(@NotNull JsonElement element) {
        return mapperManager.isJsonObject(element);
    }

    @Override
    public boolean hasField(@NotNull JsonElement element, @NotNull String field) {
        return mapperManager.hasField(element, field);
    }

    @Nullable
    @Override
    public JsonElement getField(@Nullable JsonElement element, @NotNull String field) {
        return mapperManager.getField(element, field);
    }

    @Nullable
    @Override
    public String elementToString(@Nullable JsonElement element) {
        return mapperManager.elementToString(element);
    }

    @NotNull
    @Override
    public Iterator<JsonElement> getArrayIterator(@NotNull JsonElement element, @NotNull String field) {
        return mapperManager.getArrayIterator(element, field);
    }

    @NotNull
    @Override
    public String toJson(@Nullable Object input) {
        try {
            return mapperManager.toJson(input);
        } catch (PubNubException e) {
            e.printStackTrace();
        }
        return null; //ToDo returning null is not the best solution?
    }

//    fun <T> fromJson(input: String?, clazz: Class<T>): T

    @Override
    public <T> T fromJson(String input, Class<T> clazz) {
        try {
            return mapperManager.fromJson(input, clazz);
        } catch (PubNubException e) {
            e.printStackTrace();
        }
        return null;//ToDo returning null is not the best solution?
    }
}
