package com.pubnub.api.endpoints.access.builder;

import com.pubnub.api.PubNubException;
import com.pubnub.api.UserId;
import com.pubnub.api.callbacks.PNCallback;
import com.pubnub.api.endpoints.access.GrantToken;
import com.pubnub.api.endpoints.remoteaction.RemoteAction;
import com.pubnub.api.models.consumer.access_manager.sum.SpacePermissions;
import com.pubnub.api.models.consumer.access_manager.sum.UserPermissions;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGroupGrant;
import com.pubnub.api.models.consumer.access_manager.v3.PNGrantTokenResult;
import com.pubnub.api.models.consumer.access_manager.v3.UUIDGrant;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class GrantTokenBuilder {

    private GrantTokenBuilder() {

    }

    public abstract static class Base<T> implements RemoteAction<PNGrantTokenResult> {
        protected final GrantToken grantToken;

        public Base(GrantToken grantToken) {
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

    public static class General extends Base<General> {

        public General(GrantToken grantToken) {
            super(grantToken);
        }

        public General ttl(Integer ttl) {
            grantToken.ttl(ttl);
            return this;
        }

        public General meta(Object meta) {
            grantToken.meta(meta);
            return this;
        }

        public Objects channels(List<ChannelGrant> channels) {
            return new Objects(grantToken).channels(channels);
        }

        public Objects channelGroups(List<ChannelGroupGrant> channelGroups) {
            return new Objects(grantToken).channelGroups(channelGroups);
        }

        public Objects uuids(List<UUIDGrant> uuids) {
            return new Objects(grantToken).uuids(uuids);
        }

        public Objects authorizedUUID(String authorizedUUID) {
            return new Objects(grantToken).authorizedUUID(authorizedUUID);
        }

        public SUM authorizedUserId(UserId userId) {
            return new SUM(grantToken).authorizedUserId(userId);
        }

        public SUM spacesPermissions(List<SpacePermissions> spacesPermissions) {
            return new SUM(grantToken).spacesPermissions(spacesPermissions);
        }

        public SUM usersPermissions(List<UserPermissions> usersPermissions) {
            return new SUM(grantToken).usersPermissions(usersPermissions);
        }

        @Override
        public General queryParam(Map queryParam) {
            grantToken.queryParam(queryParam);
            return this;
        }
    }

    public static class Objects extends Base<Objects> {

        public Objects(GrantToken grantToken) {
            super(grantToken);
        }

        public Objects ttl(Integer ttl) {
            grantToken.ttl(ttl);
            return this;
        }

        public Objects meta(Object meta) {
            grantToken.meta(meta);
            return this;
        }

        public Objects channels(List<ChannelGrant> channels) {
            grantToken.channels(channels);
            return this;
        }

        public Objects channelGroups(List<ChannelGroupGrant> channelGroups) {
            grantToken.channelGroups(channelGroups);
            return this;
        }

        public Objects uuids(List<UUIDGrant> uuids) {
            grantToken.uuids(uuids);
            return this;
        }

        public Objects authorizedUUID(String authorizedUUID) {
            grantToken.authorizedUUID(authorizedUUID);
            return this;
        }

        @Override
        public Objects queryParam(Map queryParam) {
            grantToken.queryParam(queryParam);
            return this;
        }
    }

    public static class SUM extends Base<SUM> {

        public SUM(GrantToken grantToken) {
            super(grantToken);
        }

        public SUM ttl(Integer ttl) {
            grantToken.ttl(ttl);
            return this;
        }

        public SUM meta(Object meta) {
            grantToken.meta(meta);
            return this;
        }

        public SUM spacesPermissions(List<SpacePermissions> spacesPermissions) {
            List<ChannelGrant> channelGrants = new ArrayList<>();
            for (SpacePermissions spacePermission : spacesPermissions) {
                final ChannelGrant channelGrant;
                if (spacePermission.isPatternResource()) {
                    channelGrant = ChannelGrant.pattern(spacePermission.getId());
                } else {
                    channelGrant = ChannelGrant.name(spacePermission.getId());
                }
                if (spacePermission.isRead()) {
                    channelGrant.read();
                }
                if (spacePermission.isWrite()) {
                    channelGrant.write();
                }
                if (spacePermission.isManage()) {
                    channelGrant.manage();
                }
                if (spacePermission.isDelete()) {
                    channelGrant.delete();
                }
                if (spacePermission.isUpdate()) {
                    channelGrant.update();
                }
                if (spacePermission.isJoin()) {
                    channelGrant.join();
                }
                if (spacePermission.isGet()) {
                    channelGrant.get();
                }
                channelGrants.add(channelGrant);
            }

            grantToken.channels(channelGrants);
            return this;
        }

        public SUM usersPermissions(List<UserPermissions> usersPermissions) {
            List<UUIDGrant> uuidsGrants = new ArrayList<>();
            for (UserPermissions userPermissions : usersPermissions) {
                final UUIDGrant channelGrant;
                if (userPermissions.isPatternResource()) {
                    channelGrant = UUIDGrant.pattern(userPermissions.getId());
                } else {
                    channelGrant = UUIDGrant.id(userPermissions.getId());
                }
                if (userPermissions.isDelete()) {
                    channelGrant.delete();
                }
                if (userPermissions.isUpdate()) {
                    channelGrant.update();
                }
                if (userPermissions.isGet()) {
                    channelGrant.get();
                }
                uuidsGrants.add(channelGrant);
            }

            grantToken.uuids(uuidsGrants);
            return this;
        }

        public SUM authorizedUserId(UserId userId) {
            grantToken.authorizedUUID(userId.getValue());
            return this;
        }

        @Override
        public SUM queryParam(Map queryParam) {
            grantToken.queryParam(queryParam);
            return this;
        }

    }
}
