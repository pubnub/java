package com.pubnub.apikt.models.consumer.objects

interface SortableKey

sealed class ResultSortKey<T : SortableKey>(
    val key: T,
    internal val dir: String = "asc"
) {
    class Asc<T : SortableKey>(key: T) : ResultSortKey<T>(key = key, dir = "asc")
    class Desc<T : SortableKey>(key: T) : ResultSortKey<T>(key = key, dir = "desc")
}
