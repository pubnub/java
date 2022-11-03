package com.pubnub.api.models.consumer.objects_api.member;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public interface PNUUID {
    @NotNull
    UUIDId getUuid();

    static PNUUID uuid(final String uuid) {
        return new UUIDWithoutCustom(new UUIDId(uuid), null);
    }

    static PNUUID uuid(final String uuid, final String status) {
        return new UUIDWithoutCustom(new UUIDId(uuid), status);
    }

    static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom), null);
    }

    static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom, final String status) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom), status);
    }

    @Data
    class UUIDId {
        private final String id;
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class UUIDWithoutCustom implements PNUUID {
        @NonNull
        private final UUIDId uuid;
        private final String status;
    }

    @Data
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    class UUIDWithCustom implements PNUUID {
        @NonNull
        private final UUIDId uuid;
        @JsonAdapter(CustomPayloadJsonInterceptor.class)
        private final Object custom;
        private final String status;
    }
}