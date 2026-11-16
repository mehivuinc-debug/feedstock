package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.FertilizingEntity
import com.example.data.model.FotoProgresEntity
import com.example.data.model.HarvestingEntity
import com.example.data.model.LahanEntity
import com.example.data.model.LandEntity
import com.example.data.model.PlantingEntity
import com.example.data.model.ProgresEntity

@Database(
    entities = [
        LahanEntity::class,
        ProgresEntity::class,
        FotoProgresEntity::class,
        LandEntity::class,
        PlantingEntity::class,
        FertilizingEntity::class,
        HarvestingEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class OdotDatabase : RoomDatabase() {
    abstract fun odotDao(): OdotDao
    abstract fun cultivationDao(): CultivationDao

    companion object {
        @Volatile
        private var INSTANCE: OdotDatabase? = null

        fun getInstance(context: Context): OdotDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    OdotDatabase::class.java,
                    "odot_track_db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
