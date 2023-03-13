package com.pubnub.apikt.models.consumer.objects

interface SortField {
    val fieldName: String
}

enum class PNKey(override val fieldName: String) : SortField {
    ID("id"),
    NAME("name"),
    UPDATED("updated"),
    TYPE("type"),
    STATUS("status");
}

enum class PNMembershipKey(override val fieldName: String) : SortField {
    CHANNEL_ID("channel.id"), CHANNEL_NAME("channel.name"), CHANNEL_UPDATED("channel.updated"), UPDATED("updated");
}

enum class PNMemberKey(override val fieldName: String) : SortField {
    UUID_ID("uuid.id"), UUID_NAME("uuid.name"), UUID_UPDATED("uuid.updated"), UPDATED("updated");
}

sealed class PNSortKey<T : SortField>(
    internal val key: T,
    internal val dir: String = "asc"
) {
    class PNAsc<T : SortField>(key: T) : PNSortKey<T>(key = key, dir = "asc")
    class PNDesc<T : SortField>(key: T) : PNSortKey<T>(key = key, dir = "desc")

    fun toSortParameter(): String {
        return key.fieldName + ":" + dir
    }

    companion object {
        fun asc(key: PNKey): PNSortKey<PNKey> {
            return PNAsc(key)
        }

        fun desc(key: PNKey): PNSortKey<PNKey> {
            return PNDesc(key)
        }
    }
}
