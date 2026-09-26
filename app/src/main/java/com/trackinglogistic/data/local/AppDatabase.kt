package com.trackinglogistic.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/** Se instancia únicamente en DatabaseModule (singleton de Hilt). */
@Database(entities = [PaqueteEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun paqueteDao(): PaqueteDao

    companion object {
        const val NOMBRE = "tracking.db"
    }
}
