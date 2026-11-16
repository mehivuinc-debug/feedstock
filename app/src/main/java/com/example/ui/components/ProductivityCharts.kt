package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.GrowthPoint
import com.example.ui.viewmodel.LahanGrowthSeries
import com.example.ui.viewmodel.LahanProductivityMetric
import com.example.util.Formatters
import kotlin.math.abs

@Composable
fun MiniRealtimeChartCard(
    growthSeries: List<LahanGrowthSeries>,
    avgProductivityKgPerM2: Double,
    avgGrowthRateCmPerDay: Double,
    onOpenFullCharts: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("mini_realtime_chart_card")
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onOpenFullCharts),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ShowChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Grafik Efisiensi Real-Time",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Laju tinggi ${Formatters.formatDecimal(avgGrowthRateCmPerDay)} cm/hari • Hasil ${Formatters.formatDecimal(avgProductivityKgPerM2)} kg/m²",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Detail",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Buka grafik lengkap",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val primarySeries = growthSeries.firstOrNull()
            if (primarySeries == null || primarySeries.points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(74.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Catat tahap Pemeliharaan (tinggi cm) untuk melihat kurva real-time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(82.dp)
                ) {
                    val leftPad = 8.dp.toPx()
                    val rightPad = 8.dp.toPx()
                    val topPad = 10.dp.toPx()
                    val bottomPad = 10.dp.toPx()
                    val plotW = (size.width - leftPad - rightPad).coerceAtLeast(1f)
                    val plotH = (size.height - topPad - bottomPad).coerceAtLeast(1f)

                    // Subtle horizontal dashed lines
                    for (i in 0..2) {
                        val y = topPad + plotH * (i / 2f)
                        drawLine(
                            color = gridColor,
                            start = Offset(leftPad, y),
                            end = Offset(size.width - rightPad, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                        )
                    }

                    val allHeights = growthSeries.flatMap { it.points.map { p -> p.tinggiCm } }
                    val maxVal = (allHeights.maxOrNull() ?: 100.0).coerceAtLeast(20.0) * 1.12

                    growthSeries.forEach { series ->
                        val pts = series.points
                        if (pts.isEmpty()) return@forEach
                        val coords = pts.mapIndexed { idx, p ->
                            val x = if (pts.size == 1) leftPad + plotW / 2f
                            else leftPad + plotW * (idx.toFloat() / (pts.size - 1).toFloat())
                            val y = topPad + plotH * (1f - (p.tinggiCm / maxVal).toFloat().coerceIn(0f, 1f))
                            Offset(x, y)
                        }

                        if (coords.size > 1) {
                            val fillPath = Path().apply {
                                moveTo(coords.first().x, topPad + plotH)
                                coords.forEach { lineTo(it.x, it.y) }
                                lineTo(coords.last().x, topPad + plotH)
                                close()
                            }
                            drawPath(
                                path = fillPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        series.color.copy(alpha = 0.22f),
                                        series.color.copy(alpha = 0.01f)
                                    ),
                                    startY = topPad,
                                    endY = topPad + plotH
                                )
                            )

                            val linePath = Path().apply {
                                moveTo(coords.first().x, coords.first().y)
                                for (i in 1 until coords.size) {
                                    val prev = coords[i - 1]
                                    val curr = coords[i]
                                    val midX = (prev.x + curr.x) / 2f
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }
                            drawPath(
                                path = linePath,
                                color = series.color,
                                style = Stroke(
                                    width = 2.5.dp.toPx(),
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }

                        coords.forEach { pt ->
                            drawCircle(color = Color.White, radius = 4.dp.toPx(), center = pt)
                            drawCircle(color = series.color, radius = 2.8.dp.toPx(), center = pt)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GrowthLineChartCard(
    growthSeries: List<LahanGrowthSeries>,
    modifier: Modifier = Modifier
) {
    var selectedPoint by remember(growthSeries) {
        mutableStateOf<Pair<Color, GrowthPoint>?>(
            growthSeries.firstOrNull()?.let { s -> s.points.lastOrNull()?.let { s.color to it } }
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("growth_line_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Kurva Pertumbuhan Tinggi (cm)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ketuk titik pada grafik untuk melihat detail ukuran",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = Emerald600
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (growthSeries.isEmpty()) {
                EmptyChartBox(
                    message = "Belum ada data pengukuran tinggi tanaman. Catat tahap Pemeliharaan dengan mengisi tinggi (cm) untuk memantau pertumbuhan."
                )
            } else {
                // Selected point inspect pill
                selectedPoint?.let { (color, pt) ->
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = color.copy(alpha = 0.12f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(color)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${pt.lahanNama} • ${Formatters.formatDateIndo(pt.tanggal)}",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = "${Formatters.formatInt(pt.tinggiCm)} cm" +
                                    (pt.umurHst?.let { " ($it HST)" } ?: ""),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = color
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                val allPoints = remember(growthSeries) {
                    growthSeries.flatMap { s -> s.points.map { s.color to it } }
                }
                val allDates = remember(growthSeries) {
                    growthSeries.flatMap { it.points.map { p -> p.tanggal } }.distinct().sorted()
                }
                val maxHeight = remember(growthSeries) {
                    (growthSeries.flatMap { it.points.map { p -> p.tinggiCm } }.maxOrNull() ?: 100.0)
                        .coerceAtLeast(20.0) * 1.15
                }
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                val labelColor = MaterialTheme.colorScheme.onSurfaceVariant

                // Y-axis + Canvas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .width(42.dp)
                            .height(160.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${Formatters.formatInt(maxHeight)} cm",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                        Text(
                            text = "${Formatters.formatInt(maxHeight / 2)} cm",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                        Text(
                            text = "0 cm",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = labelColor
                        )
                    }

                    var plottedCoords by remember { mutableStateOf<List<Triple<Offset, Color, GrowthPoint>>>(emptyList()) }

                    Canvas(
                        modifier = Modifier
                            .weight(1f)
                            .height(160.dp)
                            .pointerInput(growthSeries) {
                                detectTapGestures { tap ->
                                    val nearest = plottedCoords.minByOrNull { (pos, _, _) ->
                                        abs(pos.x - tap.x) + abs(pos.y - tap.y)
                                    }
                                    if (nearest != null) {
                                        selectedPoint = nearest.second to nearest.third
                                    }
                                }
                            }
                    ) {
                        val padX = 12.dp.toPx()
                        val padY = 10.dp.toPx()
                        val w = (size.width - padX * 2).coerceAtLeast(1f)
                        val h = (size.height - padY * 2).coerceAtLeast(1f)

                        for (i in 0..4) {
                            val y = padY + h * (i / 4f)
                            drawLine(
                                color = gridColor,
                                start = Offset(padX, y),
                                end = Offset(size.width - padX, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        val newCoords = mutableListOf<Triple<Offset, Color, GrowthPoint>>()

                        growthSeries.forEach { series ->
                            val pts = series.points
                            val coords = pts.map { pt ->
                                val dateIdx = allDates.indexOf(pt.tanggal).coerceAtLeast(0)
                                val x = if (allDates.size <= 1) {
                                    padX + w / 2f
                                } else {
                                    padX + w * (dateIdx.toFloat() / (allDates.size - 1).toFloat())
                                }
                                val y = padY + h * (1f - (pt.tinggiCm / maxHeight).toFloat().coerceIn(0f, 1f))
                                Offset(x, y) to pt
                            }

                            if (coords.size > 1) {
                                val fillPath = Path().apply {
                                    moveTo(coords.first().first.x, padY + h)
                                    coords.forEach { (off, _) -> lineTo(off.x, off.y) }
                                    lineTo(coords.last().first.x, padY + h)
                                    close()
                                }
                                drawPath(
                                    path = fillPath,
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            series.color.copy(alpha = 0.2f),
                                            Color.Transparent
                                        ),
                                        startY = padY,
                                        endY = padY + h
                                    )
                                )

                                val strokePath = Path().apply {
                                    moveTo(coords.first().first.x, coords.first().first.y)
                                    for (i in 1 until coords.size) {
                                        val p0 = coords[i - 1].first
                                        val p1 = coords[i].first
                                        val midX = (p0.x + p1.x) / 2f
                                        cubicTo(midX, p0.y, midX, p1.y, p1.x, p1.y)
                                    }
                                }
                                drawPath(
                                    path = strokePath,
                                    color = series.color,
                                    style = Stroke(
                                        width = 3.dp.toPx(),
                                        cap = StrokeCap.Round,
                                        join = StrokeJoin.Round
                                    )
                                )
                            }

                            coords.forEach { (off, pt) ->
                                val isSel = selectedPoint?.second?.progresId == pt.progresId
                                drawCircle(
                                    color = Color.White,
                                    radius = if (isSel) 6.5.dp.toPx() else 4.5.dp.toPx(),
                                    center = off
                                )
                                drawCircle(
                                    color = series.color,
                                    radius = if (isSel) 4.8.dp.toPx() else 3.2.dp.toPx(),
                                    center = off
                                )
                                newCoords.add(Triple(off, series.color, pt))
                            }
                        }

                        plottedCoords = newCoords
                    }
                }

                // X-axis date range
                if (allDates.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 42.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Formatters.formatShortDate(allDates.first()),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (allDates.size > 2) {
                            Text(
                                text = Formatters.formatShortDate(allDates[allDates.size / 2]),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (allDates.size > 1) {
                            Text(
                                text = Formatters.formatShortDate(allDates.last()),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Legend
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    growthSeries.forEach { series ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(9.dp)
                                    .clip(CircleShape)
                                    .background(series.color)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = series.lahanNama,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductivityBarChartCard(
    metrics: List<LahanProductivityMetric>,
    modifier: Modifier = Modifier
) {
    var modeEfficiency by remember { mutableStateOf(false) } // false = Total Panen (kg), true = Efisiensi (kg/m2)
    var selectedIdx by remember(metrics) { mutableIntStateOf(0) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("productivity_bar_chart_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Produktivitas Setiap Lahan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (modeEfficiency) "Efisiensi hasil panen per m² (kg/m²)"
                        else "Akumulasi bobot panen segar (kg)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Emerald700
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !modeEfficiency,
                    onClick = { modeEfficiency = false },
                    label = { Text("Total Panen (kg)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald100,
                        selectedLabelColor = Emerald700
                    ),
                    modifier = Modifier.testTag("chip_mode_total_panen")
                )
                FilterChip(
                    selected = modeEfficiency,
                    onClick = { modeEfficiency = true },
                    label = { Text("Efisiensi (kg/m²)") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Emerald100,
                        selectedLabelColor = Emerald700
                    ),
                    modifier = Modifier.testTag("chip_mode_efficiency")
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (metrics.isEmpty()) {
                EmptyChartBox(
                    message = "Belum ada data lahan atau panen. Tambahkan lahan dan catat panen untuk membandingkan produktivitas."
                )
            } else {
                val safeIdx = selectedIdx.coerceIn(0, metrics.lastIndex)
                val focused = metrics[safeIdx]

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = focused.seriesColor.copy(alpha = 0.1f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, focused.seriesColor.copy(alpha = 0.28f), RoundedCornerShape(14.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = focused.lahan.nama,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Luas ${Formatters.formatInt(focused.luasM2)} m² • ${focused.harvestCount}x panen",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = if (modeEfficiency) {
                                "${Formatters.formatDecimal(focused.efficiencyKgPerM2)} kg/m²"
                            } else {
                                "${Formatters.formatInt(focused.totalPanenKg)} kg"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = focused.seriesColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val values = metrics.map {
                    if (modeEfficiency) it.efficiencyKgPerM2 else it.totalPanenKg
                }
                val maxVal = (values.maxOrNull() ?: 1.0).coerceAtLeast(0.1)
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(metrics, modeEfficiency) {
                                detectTapGestures { tap ->
                                    val count = metrics.size.coerceAtLeast(1)
                                    val slotW = size.width / count.toFloat()
                                    val idx = (tap.x / slotW).toInt().coerceIn(0, metrics.lastIndex)
                                    selectedIdx = idx
                                }
                            }
                    ) {
                        val bottomPad = 8.dp.toPx()
                        val topPad = 14.dp.toPx()
                        val plotH = (size.height - bottomPad - topPad).coerceAtLeast(1f)

                        for (i in 0..2) {
                            val y = topPad + plotH * (i / 2f)
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f))
                            )
                        }

                        val slotW = size.width / metrics.size.toFloat()
                        val barW = (slotW * 0.46f).coerceIn(18.dp.toPx(), 54.dp.toPx())

                        metrics.forEachIndexed { idx, metric ->
                            val raw = if (modeEfficiency) metric.efficiencyKgPerM2 else metric.totalPanenKg
                            val ratio = (raw / maxVal).toFloat().coerceIn(0f, 1f)
                            val barH = (plotH * ratio).coerceAtLeast(if (raw > 0) 8.dp.toPx() else 3.dp.toPx())
                            val left = idx * slotW + (slotW - barW) / 2f
                            val top = topPad + plotH - barH

                            val isSelected = idx == safeIdx
                            drawRoundRect(
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        metric.seriesColor,
                                        metric.seriesColor.copy(alpha = if (isSelected) 0.78f else 0.55f)
                                    ),
                                    startY = top,
                                    endY = top + barH
                                ),
                                topLeft = Offset(left, top),
                                size = Size(barW, barH),
                                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx())
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    metrics.forEachIndexed { idx, metric ->
                        val valStr = if (modeEfficiency) {
                            "${Formatters.formatDecimal(metric.efficiencyKgPerM2)}"
                        } else {
                            "${Formatters.formatInt(metric.totalPanenKg)} kg"
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedIdx = idx }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = valStr,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (idx == safeIdx) metric.seriesColor else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = metric.lahan.nama,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChartBox(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
