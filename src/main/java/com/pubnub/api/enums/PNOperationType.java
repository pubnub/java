package com.pubnub.api.enums;

/**
 * Created by Max on 4/7/16.
 */
public enum PNOperationType {
    PNSubscribeOperation,
    PNUnsubscribeOperation,
    PNPublishOperation,
    PNHistoryOperation,
    PNWhereNowOperation,

    PNHeartbeatOperation,
    PNSetStateOperation,
    PNAddChannelsToGroupOperation,
    PNRemoveChannelsFromGroupOperation,
    PNChannelGroupsOperation,
    PNRemoveGroupOperation,
    PNChannelsForGroupOperation,
    PNPushNotificationEnabledChannelsOperation,
    PNAddPushNotificationsOnChannelsOperation,
    PNRemovePushNotificationsFromChannelsOperation,
    PNRemoveAllPushNotificationsOperation,
    PNTimeOperation,

    // CREATED
    PNHereNowOperation,
    PNGetState,
    PNAccessManagerAudit,
    PNAccessManagerGrant
}
