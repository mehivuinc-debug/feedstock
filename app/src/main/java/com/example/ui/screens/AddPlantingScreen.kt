package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.Agriculture
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.PhotoStorageManager
import com.example.data.model.LahanEntity
import com.example.data.model.PlantingEntity
import com.example.ui.components.ProgressPhotoImage
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.util.Formatters
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private data class PlantingDraftPhoto(
    val filePath: String,
    val caption: String
)

private fun plantingIsoToMillis(iso: String): Long? {
    return runCatching {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(iso)?.time
    }.getOrNull()
}

private fun plantingMillisToIso(millis: Long): String {
    return SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPlantingScreen(
    allLahan: List<LahanEntity>,
    preselectedLahanId: Long?,
    photoStorageManager: PhotoStorageManager,
    onNavigateBack: () -> Unit,
    onSavePlanting: (PlantingEntity, List<Pair<String, String>>) -> Unit,
    onErrorMessage: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    var selectedLahanId by remember(preselectedLahanId, allLahan) {
        mutableStateOf(preselectedLahanId ?: allLahan.firstOrNull()?.id ?: 0L)
    }
    var lahanDropdownExpanded by remember { mutableStateOf(false) }

    var plantingDate by remember { mutableStateOf(Formatters.todayIso()) }
    var showDatePicker by remember { mutableStateOf(false) }

    var seedQuantityText by remember { mutableStateOf("") }
    var seedUnit by remember { mutableStateOf("Stek") }
    var seedSource by remember { mutableStateOf("Beli") }
    var seedSourceDetail by remember { mutableStateOf("") }

    var plantingSpacing by remember { mutableStateOf("60 x 40 cm") }
    var plantingMethod by remember { mutableStateOf("Miring 45 Derajat") }
    var workerCountText by remember { mutableStateOf("2") }
    var notes by remember { mutableStateOf("") }

    val draftPhotos = remember { mutableStateListOf<PlantingDraftPhoto>() }
    var pendingTempFile by remember { mutableStateOf<File?>(null) }
    var isProcessingPhoto by remember { mutableStateOf(false) }

    var seedError by remember { mutableStateOf<String?>(null) }
    var formError by remember { mutableStateOf<String?>(null) }

    val handleCancelWithCleanup: () -> Unit = {
        if (draftPhotos.isNotEmpty()) {
            val paths = draftPhotos.map { it.filePath }
            scope.launch { photoStorageManager.deleteLocalFiles(paths) }
        }
        onNavigateBack()
    }

    BackHandler(onBack = handleCancelWithCleanup)

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = pendingTempFile
        pendingTempFile = null
        if (success && tempFile != null) {
            isProcessingPhoto = true
            scope.launch {
                val savedPath = photoStorageManager.saveCompressedPhotoFromFile(tempFile)
                isProcessingPhoto = false
                if (savedPath != null) {
                    draftPhotos.add(PlantingDraftPhoto(filePath = savedPath, caption = ""))
                } else {
                    onErrorMessage("Gagal memproses foto kamera")
                }
            }
        } else {
            tempFile?.delete()
        }
    }

    val pickGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isProcessingPhoto = true
            scope.launch {
                var added = 0
                uris.forEach { uri ->
                    val savedPath = photoStorageManager.saveCompressedPhotoFromUri(uri)
                    if (savedPath != null) {
                        draftPhotos.add(PlantingDraftPhoto(filePath = savedPath, caption = ""))
                        added++
                    }
                }
                isProcessingPhoto = false
                if (added == 0) {
                    onErrorMessage("Gagal memuat foto dari galeri")
                }
            }
        }
    }

    val selectedLahan = allLahan.firstOrNull { it.id == selectedLahanId }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.linearGradient(colors = listOf(Emerald900, Emerald700)))
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = handleCancelWithCleanup,
                        modifier = Modifier.testTag("add_planting_back_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali ke daftar lahan",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Catat Penanaman Baru",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = selectedLahan?.nama ?: "Pilih blok lahan untuk penanaman rumput odot",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                shadowElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = handleCancelWithCleanup,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_cancel_new_planting")
                    ) {
                        Text("Batal")
                    }
                    Button(
                        onClick = {
                            if (selectedLahanId == 0L) {
                                formError = "Pilih lahan target terlebih dahulu"
                                return@Button
                            }
                            val parsedQty = seedQuantityText.replace(',', '.').toDoubleOrNull()
                            if (parsedQty == null || parsedQty <= 0.0) {
                                seedError = "Masukkan jumlah bibit yang valid (> 0)"
                                return@Button
                            }
                            val entity = PlantingEntity(
                                landId = selectedLahanId,
                                plantingDate = plantingDate,
                                seedQuantity = parsedQty,
                                seedUnit = seedUnit,
                                seedSource = seedSource,
                                seedSourceDetail = seedSourceDetail.trim(),
                                plantingSpacing = plantingSpacing.trim(),
                                plantingMethod = plantingMethod,
                                workerCount = workerCountText.toIntOrNull(),
                                notes = notes.trim()
                            )
                            onSavePlanting(
                                entity,
                                draftPhotos.map { it.filePath to it.caption.trim() }
                            )
                        },
                        enabled = !isProcessingPhoto,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1.4f)
                            .height(48.dp)
                            .testTag("btn_save_new_planting")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Simpan Penanaman", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            val maxContentWidth = if (maxWidth > 680.dp) 680.dp else maxWidth

            Column(
                modifier = Modifier
                    .widthIn(max = maxContentWidth)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Section 1: Lahan & Tanggal
                PlantingSectionCard(
                    title = "Lahan & Periode Tanam",
                    subtitle = "Pilih blok lahan yang ditanami & tanggal pelaksanaan",
                    icon = Icons.Default.Landscape
                ) {
                    ExposedDropdownMenuBox(
                        expanded = lahanDropdownExpanded,
                        onExpandedChange = { lahanDropdownExpanded = !lahanDropdownExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedLahan?.let {
                                "${it.nama} (${Formatters.formatInt(it.luas)} ${it.satuan})"
                            } ?: "Pilih Lahan",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Lahan Target *") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = lahanDropdownExpanded)
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                                .testTag("planting_land_dropdown")
                        )
                        ExposedDropdownMenu(
                            expanded = lahanDropdownExpanded,
                            onDismissRequest = { lahanDropdownExpanded = false }
                        ) {
                            allLahan.forEach { lahan ->
                                DropdownMenuItem(
                                    text = {
                                        Text("${lahan.nama} (${Formatters.formatInt(lahan.luas)} ${lahan.satuan})")
                                    },
                                    onClick = {
                                        selectedLahanId = lahan.id
                                        lahanDropdownExpanded = false
                                        formError = null
                                    },
                                    modifier = Modifier.testTag("planting_land_option_${lahan.id}")
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = Formatters.formatDateIndo(plantingDate),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Tanggal Penanaman *") },
                        trailingIcon = {
                            IconButton(
                                onClick = { showDatePicker = true },
                                modifier = Modifier.testTag("planting_date_picker_btn")
                            ) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih tanggal tanam")
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showDatePicker = true }
                            .testTag("planting_date_input")
                    )
                }

                // Section 2: Data Bibit & Sumber Asal
                PlantingSectionCard(
                    title = "Jumlah Bibit & Sumber Asal",
                    subtitle = "Catat volume bibit stek dan asal perolehannya",
                    icon = Icons.Default.Eco
                ) {
                    OutlinedTextField(
                        value = seedQuantityText,
                        onValueChange = {
                            seedQuantityText = it
                            seedError = null
                            formError = null
                        },
                        label = { Text("Jumlah Bibit *") },
                        placeholder = { Text("Contoh: 2000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        isError = seedError != null,
                        supportingText = seedError?.let { msg -> { Text(msg) } },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_seed_qty_input")
                    )

                    Text("Satuan Bibit", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Stek", "Batang", "Kg").forEach { opt ->
                            FilterChip(
                                selected = seedUnit == opt,
                                onClick = { seedUnit = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald100,
                                    selectedLabelColor = Emerald700
                                ),
                                modifier = Modifier.testTag("planting_unit_chip_$opt")
                            )
                        }
                    }

                    Text("Sumber Asal Bibit *", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Beli", "Stek Sendiri", "Bantuan").forEach { opt ->
                            FilterChip(
                                selected = seedSource == opt,
                                onClick = { seedSource = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald100,
                                    selectedLabelColor = Emerald700
                                ),
                                modifier = Modifier.testTag("planting_source_chip_${opt.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = seedSourceDetail,
                        onValueChange = { seedSourceDetail = it },
                        label = { Text("Detail Pemasok / Varietas / Harga") },
                        placeholder = { Text("Misal: Indukan Blok A atau Penangkar Rp 350/stek") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_source_detail_input")
                    )
                }

                // Section 3: Teknis Penanaman
                PlantingSectionCard(
                    title = "Teknis & Jarak Tanam",
                    subtitle = "Pengaturan jarak larikan, posisi stek, dan tenaga kerja",
                    icon = Icons.Default.Agriculture
                ) {
                    Text("Preset Jarak Tanam", style = MaterialTheme.typography.labelMedium)
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("60 x 40 cm", "70 x 50 cm", "50 x 50 cm").forEach { preset ->
                            FilterChip(
                                selected = plantingSpacing == preset,
                                onClick = { plantingSpacing = preset },
                                label = { Text(preset) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald100,
                                    selectedLabelColor = Emerald700
                                )
                            )
                        }
                    }

                    OutlinedTextField(
                        value = plantingSpacing,
                        onValueChange = { plantingSpacing = it },
                        label = { Text("Jarak Tanam") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_spacing_input")
                    )

                    Text("Metode Tanam", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Miring 45 Derajat", "Tegak Lurus", "Tidur / Rebah").forEach { opt ->
                            FilterChip(
                                selected = plantingMethod == opt,
                                onClick = { plantingMethod = opt },
                                label = { Text(opt) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Emerald100,
                                    selectedLabelColor = Emerald700
                                ),
                                modifier = Modifier.testTag("planting_method_chip_${opt.lowercase().replace(" ", "_")}")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = workerCountText,
                        onValueChange = { workerCountText = it },
                        label = { Text("Jumlah Tenaga Kerja (Orang)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_worker_input")
                    )
                }

                // Section 4: Dokumentasi Foto & Catatan
                PlantingSectionCard(
                    title = "Foto Progres & Catatan Lapangan",
                    subtitle = "Lampirkan foto bedengan atau stek saat penanaman (opsional)",
                    icon = Icons.Default.PhotoLibrary
                ) {
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Catatan Kondisi Tanah / Cuaca") },
                        placeholder = { Text("Misal: Tanah gembur lembab setelah hujan ringan") },
                        minLines = 2,
                        maxLines = 4,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("planting_notes_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val (uri, tempFile) = photoStorageManager.createCameraOutputUri()
                                pendingTempFile = tempFile
                                takePictureLauncher.launch(uri)
                            },
                            enabled = !isProcessingPhoto,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("planting_camera_btn")
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil Foto")
                        }
                        OutlinedButton(
                            onClick = {
                                pickGalleryLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isProcessingPhoto,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("planting_gallery_btn")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pilih Galeri")
                        }
                    }

                    if (isProcessingPhoto) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Text("Mengompresi & menyimpan foto...", style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    if (draftPhotos.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            draftPhotos.forEachIndexed { idx, draft ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(64.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        ProgressPhotoImage(
                                            filePath = draft.filePath,
                                            contentDescription = "Preview foto tanam ${idx + 1}",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    OutlinedTextField(
                                        value = draft.caption,
                                        onValueChange = { cap -> draftPhotos[idx] = draft.copy(caption = cap) },
                                        label = { Text("Keterangan Foto ${idx + 1}") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = {
                                            val removed = draftPhotos.removeAt(idx)
                                            scope.launch {
                                                photoStorageManager.deleteLocalFiles(listOf(removed.filePath))
                                            }
                                        }
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Hapus foto ${idx + 1}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (formError != null) {
                    Text(
                        text = formError!!,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = plantingIsoToMillis(plantingDate) ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            plantingDate = plantingMillisToIso(it)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("Pilih")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Batal")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun PlantingSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Emerald100),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Emerald700,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            content()
        }
    }
}
