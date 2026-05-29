package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [Plant::class, Diagnosis::class], version = 1, exportSchema = false)
abstract class FloraDatabase : RoomDatabase() {
    abstract fun floraDao(): FloraDao

    companion object {
        @Volatile
        private var INSTANCE: FloraDatabase? = null

        fun getDatabase(context: Context): FloraDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FloraDatabase::class.java,
                    "florasense_database"
                )
                .fallbackToDestructiveMigration() // Facilitates quick schema updates during setup
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
