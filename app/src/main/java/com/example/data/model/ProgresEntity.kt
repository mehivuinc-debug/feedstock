package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "progres",
    foreignKeys = [
        ForeignKey(
            entity = LahanEntity::class,
            parentColumns = ["id"],
            childColumns = ["lahanId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["lahanId"]), Index(value = ["tanggal"])]
)
data class ProgresEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val lahanId: Long,
    val kategori: String,
    val tanggal: String,
    val catatan: String = "",
    // Stage 1: Bibit
    val jumlahBibit: Double? = null,
    val satuanBibit: String? = null,
    val sumberAsal: String? = null,
    val detailSumber: String? = null,
    // Stage 2: Penanaman
    val jarakTanam: String? = null,
    val metodeTanam: String? = null,
    val tenagaKerja: Int? = null,
    // Stage 3 & 5: Pemupukan Awal & Susulan
    val jenisPupuk: String? = null,
    val dosisKg: Double? = null,
    val caraAplikasi: String? = null,
    val susulanKe: Int? = null,
    // Stage 4: Pemeliharaan
    val umurHst: Int? = null,
    val tinggiCm: Double? = null,
    val kegiatan: String? = null,
    val kondisi: String? = null,
    // Stage 6: Panen
    val panenKe: Int? = null,
    val hasilKg: Double? = null,
    val umurPanenHari: Int? = null,
    val estimasiNilai: String? = null,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun stageType(): StageType = StageType.fromName(kategori)
}
