package com.matrix.myjournal.Entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class CombinedResponseEntity(
    @PrimaryKey(autoGenerate = true) var id: Int = 0,
    var entryDate: String,   // Use Date type for entryDate
    var entryTime: String,
    var title: String,
    var combinedResponse: String,
    var imageDataBase64: String? = null
)
