package com.pubnub.api.enums;

/**
 * Created by Max on 4/7/16.
 */
public enum PNOperationType {
    PNSubscribeOperation,
    PNUnsubscribeOperation,

    PNPublishOperation,
    PNSignalOperation,

    PNHistoryOperation,
    PNFetchMessagesOperation,
    PNDeleteMessagesOperation,
    PNMessageCountOperation,

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
    PNAccessManagerGrant,

    // UUID Metadata
    PNSetUuidMetadataOperation,
    PNGetUuidMetadataOperation,
    PNGetAllUuidMetadataOperation,
    PNRemoveUuidMetadataOperation,

    // User - VSP
    PNCreateUserOperation,
    PNFetchUserOperation,
    PNRemoveUserOperation,
    PNUpdateUserOperation,
    PNUpsertUserOperation,

    // Space - VSP
    PNCreateSpaceOperation,
    PNFetchSpaceOperation,
    PNRemoveSpaceOperation,
    PNUpdateSpaceOperation,
    PNUpsertSpaceOperation,

    // Channel Metadata
    PNSetChannelMetadataOperation,
    PNGetChannelMetadataOperation,
    PNGetAllChannelsMetadataOperation,
    PNRemoveChannelMetadataOperation,

    // Memberships
    PNSetMembershipsOperation,
    PNGetMembershipsOperation,
    PNRemoveMembershipsOperation,
    PNManageMembershipsOperation,

    // Members
    PNSetChannelMembersOperation,
    PNGetChannelMembersOperation,
    PNRemoveChannelMembersOperation,
    PNManageChannelMembersOperation,

    // PAMv3
    PNAccessManagerGrantToken,
    PNAccessManagerRevokeToken,

    // Message Actions
    PNAddMessageAction,
    PNGetMessageActions,
    PNDeleteMessageAction,

    PNFileAction
}
