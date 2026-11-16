package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "plantings",
    foreignKeys = [
        ForeignKey(
            entity = LandEntity::class,
            parentColumns = ["id"],
            childColumns = ["landId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["landId"]),
        Index(value = ["plantingDate"]),
        Index(value = ["sourceProgresId"], unique = true)
    ]
)
data class PlantingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val landId: Long,
    val sourceProgresId: Long? = null,
    val plantingDate: String,
    val seedQuantity: Double = 0.0,
    val seedUnit: String = "Stek",
    val seedSource: String = "Beli",
    val seedSourceDetail: String = "",
    val plantingSpacing: String = "",
    val plantingMethod: String = "",
    val workerCount: Int? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
