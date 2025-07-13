package com.matrix.myjournal.questionresdatabase

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.matrix.myjournal.Database.Converters
import com.matrix.myjournal.Entity.CombinedResponseEntity

@Database(entities = [ CombinedResponseEntity::class], version = 1, exportSchema = true)
@TypeConverters(Converters::class)
abstract class QuestionResDatabase : RoomDatabase() {
    abstract fun questionResDao(): QuestionResDao

    companion object {
        private var questionResDatabase: QuestionResDatabase? = null

        fun getInstance(context: Context): QuestionResDatabase {
            if (questionResDatabase == null) {
                questionResDatabase = Room.databaseBuilder(
                    context.applicationContext,
                    QuestionResDatabase::class.java,
                    "QuestionResDatabase"
                )
                    .allowMainThreadQueries()
                    .build()
            }
            return questionResDatabase!!
        }
    }
}
