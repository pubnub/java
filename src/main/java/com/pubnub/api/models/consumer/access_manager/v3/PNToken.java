package com.pubnub.api.models.consumer.access_manager.v3;

import com.pubnub.api.models.TokenBitmask;
import lombok.Data;
import lombok.NonNull;

import java.util.Map;

@Data
public class PNToken {
    private final int version;
    private final long timestamp;
    private final long ttl;
    private final String authorizedUUID;
    private final Object meta;
    @NonNull
    private final PNTokenResources resources;
    @NonNull
    private final PNTokenResources patterns;

    public static PNToken of(
            final int v,
            final long t,
            final long ttl,
            final PNTokenResources res,
            final PNTokenResources pat,
            final String uuid,
            final Object meta) {
        return new PNToken(v, t, ttl, uuid, meta, res, pat);
    }

    @Data
    public static class PNTokenResources {
        @NonNull
        private final Map<String, PNResourcePermissions> channels;
        @NonNull
        private final Map<String, PNResourcePermissions> channelGroups;
        @NonNull
        private final Map<String, PNResourcePermissions> uuids;

        public static PNTokenResources of(final Map<String, PNResourcePermissions> chan,
                                          final Map<String, PNResourcePermissions> grp,
                                          final Map<String, PNResourcePermissions> uuid) {
            return new PNTokenResources(chan, grp, uuid);
        }

    }

    @Data
    public static class PNResourcePermissions {
        private final boolean read;
        private final boolean write;
        private final boolean manage;
        private final boolean delete;
        private final boolean get;
        private final boolean update;
        private final boolean join;

        public static PNResourcePermissions of(int grant) {
            return new PNResourcePermissions(
                    (grant & TokenBitmask.READ) != 0,
                    (grant & TokenBitmask.WRITE) != 0,
                    (grant & TokenBitmask.MANAGE) != 0,
                    (grant & TokenBitmask.DELETE) != 0,
                    (grant & TokenBitmask.GET) != 0,
                    (grant & TokenBitmask.UPDATE) != 0,
                    (grant & TokenBitmask.JOIN) != 0);
        }
    }
}
