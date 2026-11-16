package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

enum class FertilizingPhase {
    INITIAL,
    FOLLOW_UP
}

@Entity(
    tableName = "fertilizings",
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
        Index(value = ["fertilizingDate"]),
        Index(value = ["sourceProgresId"], unique = true)
    ]
)
data class FertilizingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val landId: Long,
    val sourceProgresId: Long? = null,
    val phase: String = FertilizingPhase.INITIAL.name,
    val followUpSequence: Int? = null,
    val fertilizingDate: String,
    val fertilizerType: String,
    val dosageKg: Double,
    val applicationMethod: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun fertilizingPhase(): FertilizingPhase {
        return FertilizingPhase.entries.firstOrNull { it.name == phase } ?: FertilizingPhase.INITIAL
    }
}
