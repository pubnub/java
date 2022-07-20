package com.pubnub.api.endpoints.access.builder;

import com.pubnub.api.UserId;
import com.pubnub.api.endpoints.access.GrantToken;
import com.pubnub.api.models.consumer.access_manager.sum.SpacePermissions;
import com.pubnub.api.models.consumer.access_manager.sum.UserPermissions;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant;
import com.pubnub.api.models.consumer.access_manager.v3.UUIDGrant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GrantTokenEntitiesBuilder extends AbstractGrantTokenBuilder<GrantTokenEntitiesBuilder> {

    public GrantTokenEntitiesBuilder(GrantToken grantToken) {
        super(grantToken);
    }

    public GrantTokenEntitiesBuilder meta(Object meta) {
        grantToken.meta(meta);
        return this;
    }

    public GrantTokenEntitiesBuilder spacesPermissions(List<SpacePermissions> spacesPermissions) {
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

    public GrantTokenEntitiesBuilder usersPermissions(List<UserPermissions> usersPermissions) {
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

    public GrantTokenEntitiesBuilder authorizedUserId(UserId userId) {
        grantToken.authorizedUUID(userId.getValue());
        return this;
    }

    @Override
    public GrantTokenEntitiesBuilder queryParam(Map queryParam) {
        grantToken.queryParam(queryParam);
        return this;
    }

}
