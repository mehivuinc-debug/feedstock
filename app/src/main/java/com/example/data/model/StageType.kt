package com.example.data.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Lime100
import com.example.ui.theme.Lime700
import com.example.ui.theme.Orange100
import com.example.ui.theme.Orange600
import com.example.ui.theme.Purple100
import com.example.ui.theme.Purple700
import com.example.ui.theme.Sky100
import com.example.ui.theme.Sky700

enum class StageType(
    val step: Int,
    val shortLabel: String,
    val fullLabel: String,
    val photoTitle: String,
    val photoHint: String,
    val badgeBg: Color,
    val badgeContent: Color,
    val accentColor: Color
) {
    BIBIT(
        step = 1,
        shortLabel = "Bibit",
        fullLabel = "Bibit & Sumber",
        photoTitle = "Foto Bibit & Penyemaian",
        photoHint = "Foto tumpukan stek bibit, mata tunas, atau bukti pembelian bibit",
        badgeBg = Lime100,
        badgeContent = Lime700,
        accentColor = Color(0xFF65A30D)
    ),
    TANAM(
        step = 2,
        shortLabel = "Penanaman",
        fullLabel = "Periode Penanaman",
        photoTitle = "Foto Proses & Hasil Tanam",
        photoHint = "Foto bedengan/larikan tanam, jarak tanam, dan proses penanaman bibit",
        badgeBg = Emerald100,
        badgeContent = Emerald700,
        accentColor = Color(0xFF059669)
    ),
    PUPUK_AWAL(
        step = 3,
        shortLabel = "Pupuk Awal",
        fullLabel = "Pemupukan Awal",
        photoTitle = "Foto Pemupukan Awal (Dasar)",
        photoHint = "Foto pupuk dasar/kohe & dokumentasi penebaran di lahan",
        badgeBg = Amber100,
        badgeContent = Amber700,
        accentColor = Color(0xFFD97706)
    ),
    PEMELIHARAAN(
        step = 4,
        shortLabel = "Pemeliharaan",
        fullLabel = "Periode Pemeliharaan",
        photoTitle = "Foto Perkembangan Tanaman",
        photoHint = "Foto tinggi rumput, kondisi daun, penyiangan gulma, atau penyiraman",
        badgeBg = Sky100,
        badgeContent = Sky700,
        accentColor = Color(0xFF0284C7)
    ),
    PUPUK_SUSULAN(
        step = 5,
        shortLabel = "Pemupukan",
        fullLabel = "Pemupukan Susulan",
        photoTitle = "Foto Pemupukan Susulan",
        photoHint = "Foto aplikasi pupuk susulan & kondisi rumpun sebelum/sesudah pemupukan",
        badgeBg = Purple100,
        badgeContent = Purple700,
        accentColor = Color(0xFF7C3AED)
    ),
    PANEN(
        step = 6,
        shortLabel = "Panen",
        fullLabel = "Waktu & Hasil Panen",
        photoTitle = "Foto Hasil Panen Rumput Odot",
        photoHint = "Foto rumpun siap potong, hasil ikatan panen, atau penimbangan bobot",
        badgeBg = Orange100,
        badgeContent = Orange600,
        accentColor = Color(0xFFEA580C)
    );

    companion object {
        fun fromName(name: String): StageType {
            return entries.firstOrNull { it.name == name } ?: PEMELIHARAAN
        }
    }
}
