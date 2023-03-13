package com.pubnub.apikt.endpoints.objects.internal

import com.pubnub.apikt.models.consumer.objects.PNPage
import com.pubnub.apikt.models.consumer.objects.PNSortKey

data class CollectionQueryParameters(
    private val limit: Int? = null,
    private val page: PNPage? = null,
    private val filter: String? = null,
    private val sort: Collection<PNSortKey<*>> = listOf(),
    private val includeCount: Boolean = false,
) {

    internal fun createCollectionQueryParams(): Map<String, String> {
        val additionalParams = mutableMapOf<String, String>()
        val f = filter
        if (f != null) additionalParams["filter"] = f
        if (sort.isNotEmpty()) additionalParams["sort"] =
            sort.joinToString(",") { it.toSortParameter() }
        if (limit != null) additionalParams["limit"] = limit.toString()
        if (includeCount) additionalParams["count"] = includeCount.toString()
        val p = page
        when (p) {
            is PNPage.PNNext -> additionalParams["start"] = p.pageHash
            is PNPage.PNPrev -> additionalParams["end"] = p.pageHash
            null -> {}
        }
        return additionalParams.toMap()
    }
}
