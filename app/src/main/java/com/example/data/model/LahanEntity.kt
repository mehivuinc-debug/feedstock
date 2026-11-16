package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "lahan")
data class LahanEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val nama: String,
    val luas: Double,
    val satuan: String = "m²",
    val status: String = "Aktif Tanam",
    val tanggalMulai: String = "",
    val lokasi: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    fun luasDalamM2(): Double {
        return when (satuan) {
            "Hektar" -> luas * 10000.0
            "Bata" -> luas * 14.0
            else -> luas
        }
    }
}
