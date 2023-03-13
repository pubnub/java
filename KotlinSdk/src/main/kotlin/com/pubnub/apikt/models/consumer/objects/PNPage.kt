package com.pubnub.apikt.models.consumer.objects

sealed class PNPage {
    abstract val pageHash: String
    data class PNNext(override val pageHash: String) : PNPage()
    data class PNPrev(override val pageHash: String) : PNPage()
}
