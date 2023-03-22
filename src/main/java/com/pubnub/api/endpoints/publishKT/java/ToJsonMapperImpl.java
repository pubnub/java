package com.pubnub.api.endpoints.publishKT.java;

import com.pubnub.api.PubNubException;
import com.pubnub.api.endpoints.publishKT.ToJsonMapper;
import com.pubnub.api.managers.MapperManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToJsonMapperImpl implements ToJsonMapper {
    private MapperManager mapperManager;

    public ToJsonMapperImpl(MapperManager mapperManager) {
        this.mapperManager = mapperManager;
    }

    @NotNull
    @Override
    public String toJson(@Nullable Object input) {
        try {
            return mapperManager.toJson(input);
        } catch (PubNubException e) {
            e.printStackTrace();
        }
        return null;  //toDO is null the best option?
    }
}
