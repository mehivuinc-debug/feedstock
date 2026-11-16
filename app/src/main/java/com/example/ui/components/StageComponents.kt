package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ZoomOutMap
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.ProgresWithPhotos
import com.example.data.model.StageType
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Orange600
import com.example.ui.theme.Purple700
import com.example.ui.theme.Sky700
import com.example.util.Formatters

fun StageType.icon(): ImageVector = when (this) {
    StageType.BIBIT -> Icons.Default.Eco
    StageType.TANAM -> Icons.Default.Agriculture
    StageType.PUPUK_AWAL -> Icons.Default.Inventory2
    StageType.PEMELIHARAAN -> Icons.Default.FavoriteBorder
    StageType.PUPUK_SUSULAN -> Icons.Default.Bolt
    StageType.PANEN -> Icons.Default.Grass
}

@Composable
fun StageChecklistCard(
    stageMap: Map<StageType, List<ProgresWithPhotos>>,
    activeLahanLabel: String,
    onStageClick: (StageType) -> Unit,
    modifier: Modifier = Modifier
) {
    val completedCount = StageType.entries.count { !stageMap[it].isNullOrEmpty() }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("stage_checklist_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Checklist & Foto 6 Tahapan",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = activeLahanLabel,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Text(
                        text = "$completedCount/6 Tahap",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            val rows = StageType.entries.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { stage ->
                            val logs = stageMap[stage].orEmpty()
                            val hasLogs = logs.isNotEmpty()
                            val photoCount = logs.sumOf { it.fotos.size }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (hasLogs) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (hasLogs) MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
                                        else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { onStageClick(stage) }
                                    .padding(10.dp)
                                    .testTag("checklist_stage_${stage.name.lowercase()}")
                            ) {
                                Column {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "T${stage.step}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (hasLogs) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = if (photoCount > 0) Emerald600
                                            else MaterialTheme.colorScheme.surface
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                if (photoCount > 0) {
                                                    Icon(
                                                        imageVector = Icons.Default.CameraAlt,
                                                        contentDescription = null,
                                                        tint = Color.White,
                                                        modifier = Modifier.size(10.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(2.dp))
                                                    Text(
                                                        text = "$photoCount",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.White,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                } else {
                                                    Text(
                                                        text = "+Foto",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = stage.shortLabel,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = if (hasLogs) "${logs.size} catatan" else "Belum dicatat",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (hasLogs) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProgresTimelineCard(
    item: ProgresWithPhotos,
    lahanNama: String,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onPhotoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val p = item.progres
    val stage = p.stageType()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("progres_card_${p.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = stage.badgeBg
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = stage.icon(),
                                    contentDescription = null,
                                    tint = stage.badgeContent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${stage.step}. ${stage.fullLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = stage.badgeContent
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = Formatters.formatDateIndo(p.tanggal),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = lahanNama,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row {
                    IconButton(
                        onClick = onEditClick,
                        modifier = Modifier.testTag("edit_progres_${p.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit catatan progres",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("delete_progres_${p.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Hapus catatan progres",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Stage-specific structured info box
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    StageDetailsContent(item = item)
                }
            }

            if (p.catatan.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = p.catatan,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (item.fotos.isEmpty()) {
                OutlinedButton(
                    onClick = onEditClick,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_photo_quick_${p.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tambah Foto Tahapan Ini", style = MaterialTheme.typography.labelMedium)
                }
            } else if (item.fotos.size == 1) {
                val foto = item.fotos.first()
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(168.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onPhotoClick(0) }
                        .testTag("timeline_photo_${p.id}_0")
                ) {
                    ProgressPhotoImage(
                        filePath = foto.filePath,
                        contentDescription = foto.caption.ifBlank { stage.fullLabel },
                        modifier = Modifier.fillMaxSize()
                    )
                    if (foto.caption.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
                                    )
                                )
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = foto.caption,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomOutMap,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Perbesar",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White
                            )
                        }
                    }
                }
            } else {
                val rows = item.fotos.chunked(2)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    rows.forEachIndexed { rIdx, pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEachIndexed { cIdx, foto ->
                                val actualIdx = rIdx * 2 + cIdx
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(4f / 3f)
                                        .clip(RoundedCornerShape(14.dp))
                                        .clickable { onPhotoClick(actualIdx) }
                                        .testTag("timeline_photo_${p.id}_$actualIdx")
                                ) {
                                    ProgressPhotoImage(
                                        filePath = foto.filePath,
                                        contentDescription = foto.caption.ifBlank { "Foto ${actualIdx + 1}" },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(50),
                                        color = Color.Black.copy(alpha = 0.6f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = "${actualIdx + 1}/${item.fotos.size}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    if (foto.caption.isNotBlank()) {
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.62f))
                                                .padding(horizontal = 6.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = foto.caption,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StageDetailsContent(item: ProgresWithPhotos) {
    val p = item.progres
    when (p.stageType()) {
        StageType.BIBIT -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Jumlah Bibit",
                    value = "${Formatters.formatInt(p.jumlahBibit ?: 0.0)} ${p.satuanBibit ?: "Stek"}",
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Sumber Asal",
                    value = p.sumberAsal ?: "-",
                    valueColor = if (p.sumberAsal == "Beli") Amber700 else Emerald700,
                    modifier = Modifier.weight(1f)
                )
            }
            if (!p.detailSumber.isNullOrBlank()) {
                DetailMetricItem(
                    label = "Pemasok / Varietas / Harga",
                    value = p.detailSumber
                )
            }
        }
        StageType.TANAM -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Jarak Tanam",
                    value = p.jarakTanam ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Metode / Tenaga",
                    value = "${p.metodeTanam ?: "-"}" + (p.tenagaKerja?.takeIf { it > 0 }?.let { " ($it Org)" } ?: ""),
                    valueColor = Emerald700,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        StageType.PUPUK_AWAL -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Jenis Pupuk Dasar",
                    value = p.jenisPupuk ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Dosis & Aplikasi",
                    value = "${Formatters.formatInt(p.dosisKg ?: 0.0)} kg" +
                        (p.caraAplikasi?.takeIf { it.isNotBlank() }?.let { " • $it" } ?: ""),
                    valueColor = Amber700,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        StageType.PEMELIHARAAN -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Tinggi / Umur",
                    value = "${Formatters.formatInt(p.tinggiCm ?: 0.0)} cm" +
                        (p.umurHst?.takeIf { it > 0 }?.let { " ($it HST)" } ?: ""),
                    valueColor = Sky700,
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Kegiatan Perawatan",
                    value = p.kegiatan ?: "-",
                    modifier = Modifier.weight(1f)
                )
            }
            if (!p.kondisi.isNullOrBlank()) {
                DetailMetricItem(
                    label = "Kondisi Kesehatan Rumput",
                    value = p.kondisi,
                    valueColor = Emerald700
                )
            }
        }
        StageType.PUPUK_SUSULAN -> {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Pemupukan Ke-${p.susulanKe ?: 1}",
                    value = p.jenisPupuk ?: "-",
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Dosis / Takaran",
                    value = "${Formatters.formatInt(p.dosisKg ?: 0.0)} kg",
                    valueColor = Purple700,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        StageType.PANEN -> {
            val kg = p.hasilKg ?: 0.0
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                DetailMetricItem(
                    label = "Total Bobot Panen",
                    value = "${Formatters.formatInt(kg)} kg (${Formatters.formatDecimal(kg / 1000.0)} Ton)",
                    valueColor = Orange600,
                    modifier = Modifier.weight(1f)
                )
                DetailMetricItem(
                    label = "Panen Ke / Umur",
                    value = "Panen ke-${p.panenKe ?: 1} (${p.umurPanenHari ?: "-"} hari)",
                    modifier = Modifier.weight(1f)
                )
            }
            if (!p.estimasiNilai.isNullOrBlank()) {
                DetailMetricItem(
                    label = "Estimasi Nilai / Peruntukan",
                    value = p.estimasiNilai
                )
            }
        }
    }
}

@Composable
private fun DetailMetricItem(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = valueColor
        )
    }
}
