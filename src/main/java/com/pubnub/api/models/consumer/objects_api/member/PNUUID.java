package com.pubnub.api.models.consumer.objects_api.member;

import com.google.gson.annotations.JsonAdapter;
import com.pubnub.api.models.consumer.objects_api.util.CustomPayloadJsonInterceptor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class PNUUID {
    @Getter
    private final UUIDId uuid;
    @Getter
    private String status = null;

    public PNUUID(UUIDId uuid, String status) {
        this.uuid = uuid;
        this.status = status;
    }

    @AllArgsConstructor
    @EqualsAndHashCode
    @Getter
    public static class UUIDId {
        private String id;
    }

    public static PNUUID uuid(final String uuid) {
        return new UUIDWithoutCustom(new UUIDId(uuid));
    }

    public static PNUUID uuid(final String uuid, String status) {
        return new UUIDWithoutCustom(new UUIDId(uuid), status);
    }

    public static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom));
    }

    public static PNUUID uuidWithCustom(final String uuid, final Map<String, Object> custom, String status) {
        return new UUIDWithCustom(new UUIDId(uuid), new HashMap<>(custom), status);
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class UUIDWithoutCustom extends PNUUID {
        private UUIDWithoutCustom(UUIDId uuid) {
            super(uuid);
        }

        private UUIDWithoutCustom(UUIDId uuid, String status) {
            super(uuid, status);
        }
    }

    @Getter
    @EqualsAndHashCode(callSuper = true)
    public static class UUIDWithCustom extends PNUUID {
        @JsonAdapter(CustomPayloadJsonInterceptor.class)
        private final Object custom;

        private UUIDWithCustom(UUIDId uuid, Object custom) {
            super(uuid);
            this.custom = custom;
        }

        private UUIDWithCustom(UUIDId uuid, Object custom, String status) {
            super(uuid, status);
            this.custom = custom;
        }
    }
}
