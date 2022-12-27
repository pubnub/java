package com.pubnub.api.endpoints.access.builder;

import com.pubnub.api.endpoints.access.GrantToken;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGrant;
import com.pubnub.api.models.consumer.access_manager.v3.ChannelGroupGrant;
import com.pubnub.api.models.consumer.access_manager.v3.UUIDGrant;

import java.util.List;
import java.util.Map;

public class GrantTokenObjectsBuilder extends AbstractGrantTokenBuilder<GrantTokenObjectsBuilder> {

    public GrantTokenObjectsBuilder(GrantToken grantToken) {
        super(grantToken);
    }

    /**
     * @deprecated Use {@link com.pubnub.api.PubNub#grantToken(Integer)} instead.
     * @param ttl
     * @return instance of this builder
     */
    public GrantTokenObjectsBuilder ttl(Integer ttl) {
        grantToken.ttl(ttl);
        return this;
    }

    public GrantTokenObjectsBuilder meta(Object meta) {
        grantToken.meta(meta);
        return this;
    }

    public GrantTokenObjectsBuilder channels(List<ChannelGrant> channels) {
        grantToken.channels(channels);
        return this;
    }

    public GrantTokenObjectsBuilder channelGroups(List<ChannelGroupGrant> channelGroups) {
        grantToken.channelGroups(channelGroups);
        return this;
    }

    public GrantTokenObjectsBuilder uuids(List<UUIDGrant> uuids) {
        grantToken.uuids(uuids);
        return this;
    }

    public GrantTokenObjectsBuilder authorizedUUID(String authorizedUUID) {
        grantToken.authorizedUUID(authorizedUUID);
        return this;
    }

    @Override
    public GrantTokenObjectsBuilder queryParam(Map queryParam) {
        grantToken.queryParam(queryParam);
        return this;
    }
}
