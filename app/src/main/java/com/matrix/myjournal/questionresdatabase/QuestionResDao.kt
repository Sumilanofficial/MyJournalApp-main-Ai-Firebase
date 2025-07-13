package com.matrix.myjournal.questionresdatabase

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.matrix.myjournal.Entity.CombinedResponseEntity

@Dao
interface QuestionResDao {

    // Insert a new combined response
    @Insert
    fun insertCombinedResponse(combinedResponseEntity: CombinedResponseEntity)

    // Update an existing combined response
    @Update
    fun updateCombinedResponse(combinedResponseEntity: CombinedResponseEntity)


    // Get the total number of journal entries
    @Query("SELECT COUNT(*) FROM CombinedResponseEntity")
    fun getTotalJournalEntries(): LiveData<Int>

    // Get all combined responses
    @Query("SELECT * FROM CombinedResponseEntity")
    fun getCombinedResponses(): List<CombinedResponseEntity>

    // Get a combined response by its ID
    @Query("SELECT * FROM CombinedResponseEntity WHERE id = :combinedResponseId")
    fun getCombinedResponseById(combinedResponseId: Int): CombinedResponseEntity?

    // Delete a combined response by its ID
    @Query("DELETE FROM CombinedResponseEntity WHERE id = :combinedResponseId")
    fun deleteCombinedResponse(combinedResponseId: Int)

    @Query("SELECT COUNT(*) FROM CombinedResponseEntity")
    fun getTotalNumberOfJournals(): LiveData<Int>


    // Get word count per day, grouped by entryDate
    @Query("""
        SELECT entryDate, 
               SUM(LENGTH(combinedResponse) - LENGTH(REPLACE(combinedResponse, ' ', '')) + 1) AS wordCount 
        FROM CombinedResponseEntity 
        GROUP BY entryDate
    """)
    fun getWordCountPerDay(): List<WordCountPerDay>

    // Get the total number of images in entries where imageDataBase64 is not null or empty
    @Query("""
        SELECT COUNT(*) 
        FROM CombinedResponseEntity 
        WHERE imageDataBase64 IS NOT NULL AND imageDataBase64 <> ''
    """)
    fun getTotalImagesCount(): LiveData<Int>

    // Get the total word count across all combined responses
    @Query("""
        SELECT SUM(LENGTH(combinedResponse) - LENGTH(REPLACE(combinedResponse, ' ', '')) + 1) 
        FROM CombinedResponseEntity
    """)
    fun getTotalWordCount(): LiveData<Int>

    // Get the total streaks (number of unique dates with entries)
    @Query("""
        SELECT COUNT(*) 
        FROM (
            SELECT COUNT(*) 
            FROM CombinedResponseEntity 
            GROUP BY strftime('%Y-%m-%d', entryDate)
        )
    """)
    fun getTotalStreaks(): LiveData<Int>
}
data class WordCountPerDay(
    val entryDate: String,
    val wordCount: Int
)
