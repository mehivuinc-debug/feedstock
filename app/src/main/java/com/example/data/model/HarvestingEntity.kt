package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "harvestings",
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
        Index(value = ["harvestDate"]),
        Index(value = ["sourceProgresId"], unique = true)
    ]
)
data class HarvestingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val landId: Long,
    val sourceProgresId: Long? = null,
    val harvestNumber: Int = 1,
    val harvestDate: String,
    val yieldKg: Double,
    val harvestAgeDays: Int? = null,
    val estimatedValueOrUsage: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class LandWithCultivationRecords(
    @Embedded val land: LandEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "landId"
    )
    val plantings: List<PlantingEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "landId"
    )
    val fertilizings: List<FertilizingEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "landId"
    )
    val harvestings: List<HarvestingEntity>
)
