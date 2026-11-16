package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lands")
data class LandEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val areaSize: Double,
    val areaUnit: String = "m²",
    val status: String = "Aktif Tanam",
    val startDate: String = "",
    val locationNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun areaInSquareMeters(): Double {
        return when (areaUnit) {
            "Hektar" -> areaSize * 10000.0
            "Bata" -> areaSize * 14.0
            else -> areaSize
        }
    }
}
