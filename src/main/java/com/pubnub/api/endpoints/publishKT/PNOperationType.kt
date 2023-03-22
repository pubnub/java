package com.pubnub.api.endpoints.publishKT

sealed class PNOperationType(open val queryParam: String? = null) {

    open class PublishOperation : PNOperationType("pub")
    open class HistoryOperation : PNOperationType("hist")
    open class PresenceOperation : PNOperationType("pres")
    open class ChannelGroupOperation : PNOperationType("cg")
    open class PushNotificationsOperation : PNOperationType("push")
    open class PAMOperation : PNOperationType("pam")
    open class MessageCountsOperation : PNOperationType("mc")
    open class SignalsOperation : PNOperationType("sig")
    open class ObjectsOperation : PNOperationType("obj")
    open class PAMV3Operation : PNOperationType("pamv3")
    open class MessageActionsOperation : PNOperationType("msga")
    open class TimeOperation : PNOperationType("time")
    object FileOperation : PNOperationType("file")
    object SpaceOperation : PNOperationType("obj")
    object UserOperation : PNOperationType("obj")
    object MembershipOperation : PNOperationType("obj")

    object PNSubscribeOperation : PNOperationType()

    object PNPublishOperation : PublishOperation()

    object PNHistoryOperation : HistoryOperation()
    object PNFetchMessagesOperation : HistoryOperation()
    object PNDeleteMessagesOperation : HistoryOperation()

    object PNUnsubscribeOperation : PresenceOperation()
    object PNWhereNowOperation : PresenceOperation()
    object PNHereNowOperation : PresenceOperation()
    object PNHeartbeatOperation : PresenceOperation()
    object PNSetStateOperation : PresenceOperation()
    object PNGetState : PresenceOperation()

    object PNAddChannelsToGroupOperation : ChannelGroupOperation()
    object PNRemoveChannelsFromGroupOperation : ChannelGroupOperation()
    object PNChannelGroupsOperation : ChannelGroupOperation()
    object PNRemoveGroupOperation : ChannelGroupOperation()
    object PNChannelsForGroupOperation : ChannelGroupOperation()

    object PNPushNotificationEnabledChannelsOperation : PushNotificationsOperation()
    object PNAddPushNotificationsOnChannelsOperation : PushNotificationsOperation()
    object PNRemovePushNotificationsFromChannelsOperation : PushNotificationsOperation()
    object PNRemoveAllPushNotificationsOperation : PushNotificationsOperation()

    object PNAccessManagerAudit : PAMOperation()
    object PNAccessManagerGrant : PAMOperation()

    object PNMessageCountOperation : MessageCountsOperation()

    object PNSignalOperation : SignalsOperation()

    object PNSetUUIDMetadataOperation : ObjectsOperation()
    object PNGetUUIDMetadataOperation : ObjectsOperation()
    object PNGetAllUUIDMetadataOperation : ObjectsOperation()
    object PNRemoveUUIDMetadataOperation : ObjectsOperation()
    object PNSetChannelMetadataOperation : ObjectsOperation()
    object PNGetChannelMetadataOperation : ObjectsOperation()
    object PNGetAllChannelsMetadataOperation : ObjectsOperation()
    object PNRemoveChannelMetadataOperation : ObjectsOperation()
    object PNGetMembershipsOperation : ObjectsOperation()
    object PNSetMembershipsOperation : ObjectsOperation()
    object PNUpdateMembershipsOperation : ObjectsOperation()
    object PNManageMemberships : ObjectsOperation()

    object PNAccessManagerGrantToken : PAMV3Operation()
    object PNAccessManagerRevokeToken : PAMV3Operation()

    object PNAddMessageAction : MessageActionsOperation()
    object PNGetMessageActions : MessageActionsOperation()
    object PNDeleteMessageAction : MessageActionsOperation()

    object PNTimeOperation : TimeOperation()

    override fun toString(): String {
        return this.javaClass.simpleName
    }
}
