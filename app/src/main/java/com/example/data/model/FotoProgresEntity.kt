package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(
    tableName = "foto_progres",
    foreignKeys = [
        ForeignKey(
            entity = ProgresEntity::class,
            parentColumns = ["id"],
            childColumns = ["progresId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["progresId"])]
)
data class FotoProgresEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val progresId: Long,
    val filePath: String,
    val caption: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class ProgresWithPhotos(
    @Embedded val progres: ProgresEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "progresId"
    )
    val fotos: List<FotoProgresEntity>
)
