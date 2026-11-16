package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Grass
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.model.ProgresWithPhotos
import com.example.data.model.StageType
import com.example.ui.components.MiniRealtimeChartCard
import com.example.ui.components.ProgresTimelineCard
import com.example.ui.components.StageChecklistCard
import com.example.ui.theme.Amber100
import com.example.ui.theme.Amber700
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Lime100
import com.example.ui.theme.Lime700
import com.example.ui.theme.Orange100
import com.example.ui.theme.Orange600
import com.example.ui.viewmodel.OdotUiState
import com.example.util.Formatters

@Composable
fun BerandaScreen(
    uiState: OdotUiState,
    onStageChecklistClick: (StageType) -> Unit,
    onStageFilterClick: (StageType?) -> Unit,
    onOpenFullCharts: () -> Unit,
    onAddProgresClick: () -> Unit,
    onEditProgresClick: (ProgresWithPhotos) -> Unit,
    onDeleteProgresClick: (Long) -> Unit,
    onOpenLightbox: (ProgresWithPhotos, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeLahanName = uiState.activeLahanId?.let { id ->
        uiState.allLahan.firstOrNull { it.id == id }?.nama
    } ?: "Semua Lahan (${uiState.allLahan.size} Blok)"

    val lahanLookup = uiState.allLahan.associateBy { it.id }

    BoxWithConstraints(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        val maxContentWidth = if (maxWidth > 720.dp) 720.dp else maxWidth

        LazyColumn(
            modifier = Modifier
                .widthIn(max = maxContentWidth)
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Hero Banner Card
            item {
                HeroBannerCard(
                    activeLahanLabel = activeLahanName,
                    totalLogCount = uiState.filteredProgresForTimeline.size,
                    totalPhotos = uiState.galleryPhotos.size
                )
            }

            // 4 KPI Cards (2x2 Grid)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMetricCard(
                            title = "Total Luas Lahan",
                            value = "${Formatters.formatInt(uiState.totalLuasM2)} m²",
                            subtitle = "${uiState.filteredLahan.size} blok aktif dipantau",
                            icon = Icons.Default.Landscape,
                            iconBg = Emerald100,
                            iconTint = Emerald700,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_luas_lahan")
                        )
                        KpiMetricCard(
                            title = "Total Bibit Ditanam",
                            value = "${Formatters.formatInt(uiState.totalBibit)} Stek",
                            subtitle = "Beli & stek kebun sendiri",
                            icon = Icons.Default.Eco,
                            iconBg = Lime100,
                            iconTint = Lime700,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_total_bibit")
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        KpiMetricCard(
                            title = "Akumulasi Panen",
                            value = "${Formatters.formatInt(uiState.totalPanenKg)} kg",
                            subtitle = "${Formatters.formatDecimal(uiState.totalPanenKg / 1000.0)} Ton hijauan",
                            icon = Icons.Default.Grass,
                            iconBg = Orange100,
                            iconTint = Orange600,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_total_panen")
                        )
                        KpiMetricCard(
                            title = "Efisiensi Lahan",
                            value = "${Formatters.formatDecimal(uiState.avgProductivityKgPerM2)} kg/m²",
                            subtitle = if (uiState.avgCycleDays > 0) "Rata-rata ${Formatters.formatInt(uiState.avgCycleDays)} hari panen" else "Menunggu data panen",
                            icon = Icons.Default.Speed,
                            iconBg = Amber100,
                            iconTint = Amber700,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("kpi_efisiensi")
                        )
                    }
                }
            }

            // 6-Stage Checklist & Photo Counter
            item {
                StageChecklistCard(
                    stageMap = uiState.filteredStageMap,
                    activeLahanLabel = activeLahanName,
                    onStageClick = onStageChecklistClick
                )
            }

            // Mini Real-Time Chart
            item {
                MiniRealtimeChartCard(
                    growthSeries = uiState.growthSeries,
                    avgProductivityKgPerM2 = uiState.avgProductivityKgPerM2,
                    avgGrowthRateCmPerDay = uiState.avgGrowthRateCmPerDay,
                    onOpenFullCharts = onOpenFullCharts
                )
            }

            // Timeline Filter Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Riwayat & Foto Progres",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${uiState.filteredProgresForTimeline.size} catatan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = uiState.activeStageFilter == null,
                            onClick = { onStageFilterClick(null) },
                            label = { Text("Semua") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald100,
                                selectedLabelColor = Emerald700
                            ),
                            modifier = Modifier.testTag("timeline_filter_all")
                        )
                        StageType.entries.forEach { stage ->
                            FilterChip(
                                selected = uiState.activeStageFilter == stage,
                                onClick = {
                                    onStageFilterClick(if (uiState.activeStageFilter == stage) null else stage)
                                },
                                label = { Text("${stage.step}. ${stage.shortLabel}") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = stage.badgeBg,
                                    selectedLabelColor = stage.badgeContent
                                ),
                                modifier = Modifier.testTag("timeline_filter_${stage.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Timeline List or Empty State
            if (uiState.filteredProgresForTimeline.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Belum ada catatan progres",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Mulai dokumentasikan bibit, tanam, pemupukan, pemeliharaan tinggi tanaman, dan hasil panen rumput odot.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(
                                onClick = onAddProgresClick,
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("empty_add_progres_btn")
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Catat Progres Pertama")
                            }
                        }
                    }
                }
            } else {
                items(
                    items = uiState.filteredProgresForTimeline,
                    key = { it.progres.id }
                ) { item ->
                    val lahanNama = lahanLookup[item.progres.lahanId]?.nama ?: "Lahan"
                    ProgresTimelineCard(
                        item = item,
                        lahanNama = lahanNama,
                        onEditClick = { onEditProgresClick(item) },
                        onDeleteClick = { onDeleteProgresClick(item.progres.id) },
                        onPhotoClick = { photoIdx -> onOpenLightbox(item, photoIdx) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBannerCard(
    activeLahanLabel: String,
    totalLogCount: Int,
    totalPhotos: Int
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(148.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_odot_banner),
                contentDescription = "Ilustrasi perkebunan rumput odot hijau subur",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF064E3B).copy(alpha = 0.88f),
                                Color(0xFF064E3B).copy(alpha = 0.45f)
                            )
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Surface(
                    shape = RoundedCornerShape(50),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = activeLahanLabel,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
                Column {
                    Text(
                        text = "Pantau Pertumbuhan Real-Time",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalLogCount catatan budidaya • $totalPhotos foto lapangan terdokumentasi",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
