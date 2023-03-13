package com.pubnub.apikt.legacy.endpoints.files

import org.junit.jupiter.api.BeforeEach
import java.util.*

interface TestsWithFiles {

    fun fileName() = _fileName

    fun inputStream(content: String = "content") = content.byteInputStream()

    @BeforeEach
    fun beforeEach() {
        _fileName = "file_${UUID.randomUUID()}"
    }

    companion object {
        private lateinit var _fileName: String
    }
}
