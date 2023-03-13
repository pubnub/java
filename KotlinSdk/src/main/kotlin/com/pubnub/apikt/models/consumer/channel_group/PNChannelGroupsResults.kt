package com.pubnub.apikt.models.consumer.channel_group

import com.pubnub.apikt.PubNub

/**
 * Result of the [PubNub.addChannelsToChannelGroup] operation.
 */
class PNChannelGroupsAddChannelResult

/**
 * Result of the [PubNub.deleteChannelGroup] operation.
 */
class PNChannelGroupsDeleteGroupResult

/**
 * Result of the [PubNub.removeChannelsFromChannelGroup] operation.
 */
class PNChannelGroupsRemoveChannelResult

/**
 * Result of the [PubNub.listChannelsForChannelGroup] operation.
 *
 * @property channels List of channels belonging to a channel group.
 */
class PNChannelGroupsAllChannelsResult internal constructor(
    val channels: List<String>
)

/**
 * Result of the [PubNub.listAllChannelGroups] operation.
 *
 * @property groups List of all channel groups
 */
class PNChannelGroupsListAllResult internal constructor(
    val groups: List<String>
)
