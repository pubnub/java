package com.pubnub.api.models.consumer.objects_api.member;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

import java.util.HashMap;
import java.util.Map;

@Data
public abstract class PNUUID {
    @NonNull
    private final UUIDId uuid;
    private final String status;

    public static PNUUID uuid(final String uuid) {
        return new UUIDWithoutCustom(new UUIDId(uuid), null);
    }

    public static PNUUID uuid(final String uuid, final String status) {
        return new UUIDWithoutCustom(new UUIDId(uuid), status);
    }

    public static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom), null);
    }

    public static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom, final String status) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom), status);
    }

    @Data
    public static class UUIDId {
        private final String id;
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString
    private static class UUIDWithoutCustom extends PNUUID {
        private UUIDWithoutCustom(UUIDId uuid, String status) {
            super(uuid, status);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    @ToString
    public static class UUIDWithCustom extends PNUUID {
        @JsonAdapter(CustomPayloadJsonInterceptor.class)
        private final Object custom;

        private UUIDWithCustom(UUIDId uuid, Object custom, String status) {
            super(uuid, status);
            this.custom = custom;
        }
    }
}