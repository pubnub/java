package com.pubnub.apikt.models.server.access_manager.v3

import com.pubnub.apikt.PubNubException
import com.pubnub.apikt.models.TokenBitmask
import com.pubnub.apikt.models.consumer.access_manager.v3.*

data class GrantTokenRequestBody(
    val ttl: Int,
    val permissions: GrantTokenPermissions
) {

    data class GrantTokenPermissions(
        val resources: GrantTokenPermission,
        val patterns: GrantTokenPermission,
        val meta: Any? = null,
        val uuid: String? = null
    )

    data class GrantTokenPermission(
        val channels: Map<String, Int> = emptyMap(),
        val groups: Map<String, Int> = emptyMap(),
        val uuids: Map<String, Int> = emptyMap(),
        val spaces: Map<String, Int> = emptyMap(),
        val users: Map<String, Int> = emptyMap()
    )

    companion object {
        @Throws(PubNubException::class)
        fun of(
            ttl: Int,
            channels: List<ChannelGrant>,
            groups: List<ChannelGroupGrant>,
            uuids: List<UUIDGrant>,
            meta: Any?,
            uuid: String?
        ): GrantTokenRequestBody {
            val resources = GrantTokenPermission(
                getResources(channels),
                getResources(groups), getResources(uuids)
            )
            val patterns = GrantTokenPermission(
                getPatterns(channels),
                getPatterns(groups), getPatterns(uuids)
            )
            val permissions = GrantTokenPermissions(resources, patterns, meta ?: emptyMap<Any, Any>(), uuid)
            return GrantTokenRequestBody(ttl, permissions)
        }

        private fun <T : PNGrant> getResources(resources: List<T>): Map<String, Int> {
            return resources
                .filter { it !is PNPatternGrant }
                .associate { it.id to calculateBitmask(it) }
        }

        private fun <T : PNGrant> getPatterns(resources: List<T>): Map<String, Int> {
            return resources
                .filterIsInstance(PNPatternGrant::class.java)
                .associate { it.id to calculateBitmask(it) }
        }

        private fun calculateBitmask(resource: PNGrant): Int {
            var sum = 0
            if (resource.read) {
                sum = sum or TokenBitmask.READ
            }
            if (resource.write) {
                sum = sum or TokenBitmask.WRITE
            }
            if (resource.manage) {
                sum = sum or TokenBitmask.MANAGE
            }
            if (resource.delete) {
                sum = sum or TokenBitmask.DELETE
            }
            if (resource.create) {
                sum = sum or TokenBitmask.CREATE
            }
            if (resource.get) {
                sum = sum or TokenBitmask.GET
            }
            if (resource.join) {
                sum = sum or TokenBitmask.JOIN
            }
            if (resource.update) {
                sum = sum or TokenBitmask.UPDATE
            }
            return sum
        }
    }
}
