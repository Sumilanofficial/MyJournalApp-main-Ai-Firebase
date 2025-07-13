package com.matrix.myjournal.DataClasses

import androidx.annotation.Keep

@Keep
data class JournalEntry(
    var id: String? = null,
    var title: String? = "",
    var combinedResponse: String? = "",
    var entryDate: String? = "",
    var entryTime: String? = "",
    var imageUrls: List<String>? = emptyList()
)
