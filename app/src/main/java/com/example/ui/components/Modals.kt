package com.example.ui.components

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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.local.PhotoStorageManager
import com.example.data.model.LahanEntity
import com.example.data.model.ProgresEntity
import com.example.data.model.ProgresWithPhotos
import com.example.data.model.StageType
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.viewmodel.ConfirmDialogState
import com.example.ui.viewmodel.LightboxState
import com.example.util.Formatters
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

private fun isoDateToMillis(iso: String): Long? {
    return runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        sdf.parse(iso)?.time
    }.getOrNull()
}

private fun millisToIsoDate(millis: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }
    return sdf.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OdotDatePickerModal(
    initialIsoDate: String,
    onDateSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = isoDateToMillis(initialIsoDate) ?: System.currentTimeMillis()
    )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = datePickerState.selectedDateMillis
                    if (millis != null) {
                        onDateSelected(millisToIsoDate(millis))
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("date_picker_confirm")
            ) {
                Text("Pilih")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("date_picker_cancel")
            ) {
                Text("Batal")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LahanFormBottomSheet(
    editingLahan: LahanEntity?,
    onDismiss: () -> Unit,
    onSave: (LahanEntity) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isEdit = editingLahan != null

    var nama by remember(editingLahan) { mutableStateOf(editingLahan?.nama ?: "") }
    var luasText by remember(editingLahan) {
        mutableStateOf(
            editingLahan?.luas?.let {
                if (it % 1.0 == 0.0) it.toInt().toString() else it.toString()
            } ?: ""
        )
    }
    var satuan by remember(editingLahan) { mutableStateOf(editingLahan?.satuan ?: "m²") }
    var status by remember(editingLahan) { mutableStateOf(editingLahan?.status ?: "Aktif Tanam") }
    var tanggalMulai by remember(editingLahan) {
        mutableStateOf(editingLahan?.tanggalMulai?.ifBlank { Formatters.todayIso() } ?: Formatters.todayIso())
    }
    var lokasi by remember(editingLahan) { mutableStateOf(editingLahan?.lokasi ?: "") }

    var namaError by remember { mutableStateOf<String?>(null) }
    var luasError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val satuanOptions = listOf("m²", "Hektar", "Bata")
    val statusOptions = listOf(
        "Aktif Tanam",
        "Persiapan Lahan",
        "Siap Panen",
        "Istirahat / Pasca Panen"
    )

    BackHandler(onBack = onDismiss)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEdit) "Edit Data Lahan" else "Tambah Lahan Baru",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Catat ukuran & lokasi blok penanaman rumput odot",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_lahan_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup form lahan")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            OutlinedTextField(
                value = nama,
                onValueChange = {
                    nama = it
                    namaError = null
                },
                label = { Text("Nama / Blok Lahan *") },
                placeholder = { Text("Contoh: Blok C - Dekat Sumber Air") },
                isError = namaError != null,
                supportingText = namaError?.let { msg -> { Text(msg) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lahan_nama_input")
            )

            OutlinedTextField(
                value = luasText,
                onValueChange = {
                    luasText = it
                    luasError = null
                },
                label = { Text("Luas Lahan *") },
                placeholder = { Text("Contoh: 1200") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                isError = luasError != null,
                supportingText = luasError?.let { msg -> { Text(msg) } },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lahan_luas_input")
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Satuan Luas",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    satuanOptions.forEach { option ->
                        FilterChip(
                            selected = satuan == option,
                            onClick = { satuan = option },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald100,
                                selectedLabelColor = Emerald700
                            ),
                            modifier = Modifier.testTag("satuan_chip_$option")
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Status Lahan Saat Ini",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    statusOptions.forEach { option ->
                        FilterChip(
                            selected = status == option,
                            onClick = { status = option },
                            label = { Text(option) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Emerald100,
                                selectedLabelColor = Emerald700
                            ),
                            modifier = Modifier.testTag("status_chip_${option.lowercase().replace(" ", "_")}")
                        )
                    }
                }
            }

            OutlinedTextField(
                value = Formatters.formatDateIndo(tanggalMulai),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Mulai Kelola") },
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.testTag("lahan_date_picker_btn")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih tanggal mulai")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .testTag("lahan_tanggal_input")
            )

            OutlinedTextField(
                value = lokasi,
                onValueChange = { lokasi = it },
                label = { Text("Lokasi & Kondisi Tanah") },
                placeholder = { Text("Jenis tanah, akses irigasi, atau catatan blok") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("lahan_lokasi_input")
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_lahan_button")
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        val trimmedName = nama.trim()
                        val parsedLuas = luasText.replace(',', '.').toDoubleOrNull()
                        if (trimmedName.isBlank()) {
                            namaError = "Nama lahan wajib diisi"
                            return@Button
                        }
                        if (parsedLuas == null || parsedLuas <= 0.0) {
                            luasError = "Masukkan luas lahan yang valid (> 0)"
                            return@Button
                        }
                        onSave(
                            LahanEntity(
                                id = editingLahan?.id ?: 0L,
                                nama = trimmedName,
                                luas = parsedLuas,
                                satuan = satuan,
                                status = status,
                                tanggalMulai = tanggalMulai,
                                lokasi = lokasi.trim(),
                                createdAt = editingLahan?.createdAt ?: System.currentTimeMillis()
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_lahan_button")
                ) {
                    Text(if (isEdit) "Simpan Perubahan" else "Simpan Lahan")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        OdotDatePickerModal(
            initialIsoDate = tanggalMulai,
            onDateSelected = { tanggalMulai = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

private data class DraftPhoto(
    val filePath: String,
    val caption: String,
    val isExisting: Boolean
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProgresFormBottomSheet(
    allLahan: List<LahanEntity>,
    activeLahanId: Long?,
    editingProgres: ProgresWithPhotos?,
    presetStage: StageType,
    photoStorageManager: PhotoStorageManager,
    onDismiss: () -> Unit,
    onSave: (ProgresEntity, List<Pair<String, String>>, List<String>) -> Unit,
    onErrorMessage: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val isEdit = editingProgres != null
    val initialProgres = editingProgres?.progres

    var selectedLahanId by remember(editingProgres, activeLahanId, allLahan) {
        mutableStateOf(
            initialProgres?.lahanId
                ?: activeLahanId
                ?: allLahan.firstOrNull()?.id
                ?: 0L
        )
    }
    var lahanMenuExpanded by remember { mutableStateOf(false) }

    var selectedStage by remember(editingProgres, presetStage) {
        mutableStateOf(initialProgres?.stageType() ?: presetStage)
    }
    var tanggal by remember(editingProgres) {
        mutableStateOf(initialProgres?.tanggal ?: Formatters.todayIso())
    }
    var catatan by remember(editingProgres) {
        mutableStateOf(initialProgres?.catatan ?: "")
    }

    // Stage 1: Bibit
    var jumlahBibitText by remember(editingProgres) {
        mutableStateOf(initialProgres?.jumlahBibit?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var satuanBibit by remember(editingProgres) { mutableStateOf(initialProgres?.satuanBibit ?: "Stek") }
    var sumberAsal by remember(editingProgres) { mutableStateOf(initialProgres?.sumberAsal ?: "Beli") }
    var detailSumber by remember(editingProgres) { mutableStateOf(initialProgres?.detailSumber ?: "") }

    // Stage 2: Penanaman
    var jarakTanam by remember(editingProgres) { mutableStateOf(initialProgres?.jarakTanam ?: "60 x 40 cm") }
    var metodeTanam by remember(editingProgres) { mutableStateOf(initialProgres?.metodeTanam ?: "Miring 45 Derajat") }
    var tenagaKerjaText by remember(editingProgres) { mutableStateOf(initialProgres?.tenagaKerja?.toString() ?: "") }

    // Stage 3 & 5: Pemupukan
    var jenisPupuk by remember(editingProgres) { mutableStateOf(initialProgres?.jenisPupuk ?: "") }
    var dosisKgText by remember(editingProgres) {
        mutableStateOf(initialProgres?.dosisKg?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var caraAplikasi by remember(editingProgres) { mutableStateOf(initialProgres?.caraAplikasi ?: "Tebar di Larikan Tanam") }
    var susulanKeText by remember(editingProgres) { mutableStateOf(initialProgres?.susulanKe?.toString() ?: "1") }

    // Stage 4: Pemeliharaan
    var umurHstText by remember(editingProgres) { mutableStateOf(initialProgres?.umurHst?.toString() ?: "") }
    var tinggiCmText by remember(editingProgres) {
        mutableStateOf(initialProgres?.tinggiCm?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var kegiatan by remember(editingProgres) { mutableStateOf(initialProgres?.kegiatan ?: "Kontrol Rutin") }
    var kondisi by remember(editingProgres) { mutableStateOf(initialProgres?.kondisi ?: "") }

    // Stage 6: Panen
    var panenKeText by remember(editingProgres) { mutableStateOf(initialProgres?.panenKe?.toString() ?: "1") }
    var hasilKgText by remember(editingProgres) {
        mutableStateOf(initialProgres?.hasilKg?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "")
    }
    var umurPanenHariText by remember(editingProgres) { mutableStateOf(initialProgres?.umurPanenHari?.toString() ?: "") }
    var estimasiNilai by remember(editingProgres) { mutableStateOf(initialProgres?.estimasiNilai ?: "") }

    // Photos state
    val draftPhotos = remember(editingProgres) {
        mutableStateListOf<DraftPhoto>().apply {
            editingProgres?.fotos?.forEach {
                add(DraftPhoto(filePath = it.filePath, caption = it.caption, isExisting = true))
            }
        }
    }
    val removedExistingPaths = remember(editingProgres) { mutableStateListOf<String>() }
    val newlyCreatedPaths = remember(editingProgres) { mutableStateListOf<String>() }
    var isProcessingPhoto by remember { mutableStateOf(false) }
    var pendingCameraTempFile by remember { mutableStateOf<File?>(null) }

    var formError by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val dismissWithCleanup: () -> Unit = {
        if (newlyCreatedPaths.isNotEmpty()) {
            val pathsToClean = newlyCreatedPaths.toList()
            scope.launch { photoStorageManager.deleteLocalFiles(pathsToClean) }
        }
        onDismiss()
    }

    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        val tempFile = pendingCameraTempFile
        pendingCameraTempFile = null
        if (success && tempFile != null) {
            isProcessingPhoto = true
            scope.launch {
                val savedPath = photoStorageManager.saveCompressedPhotoFromFile(tempFile)
                isProcessingPhoto = false
                if (savedPath != null) {
                    draftPhotos.add(DraftPhoto(filePath = savedPath, caption = "", isExisting = false))
                    newlyCreatedPaths.add(savedPath)
                } else {
                    onErrorMessage("Gagal memproses foto kamera")
                }
            }
        } else {
            tempFile?.delete()
        }
    }

    val pickMultipleMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            isProcessingPhoto = true
            scope.launch {
                var addedCount = 0
                uris.forEach { uri ->
                    val savedPath = photoStorageManager.saveCompressedPhotoFromUri(uri)
                    if (savedPath != null) {
                        draftPhotos.add(DraftPhoto(filePath = savedPath, caption = "", isExisting = false))
                        newlyCreatedPaths.add(savedPath)
                        addedCount++
                    }
                }
                isProcessingPhoto = false
                if (addedCount == 0) {
                    onErrorMessage("Gagal memuat foto dari galeri")
                }
            }
        }
    }

    BackHandler(onBack = dismissWithCleanup)

    ModalBottomSheet(
        onDismissRequest = dismissWithCleanup,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEdit) "Edit Catatan Progres" else "Catat Progres Budidaya",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Dokumentasikan tahapan budidaya rumput odot & foto lapangan",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(
                    onClick = dismissWithCleanup,
                    modifier = Modifier.testTag("close_progres_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Tutup form progres")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))

            // Lahan selector
            val selectedLahanObj = allLahan.firstOrNull { it.id == selectedLahanId }
            ExposedDropdownMenuBox(
                expanded = lahanMenuExpanded,
                onExpandedChange = { lahanMenuExpanded = !lahanMenuExpanded }
            ) {
                OutlinedTextField(
                    value = selectedLahanObj?.let { "${it.nama} (${Formatters.formatInt(it.luas)} ${it.satuan})" }
                        ?: "Pilih Lahan",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Lahan Target *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = lahanMenuExpanded) },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                        .testTag("progres_lahan_dropdown")
                )
                ExposedDropdownMenu(
                    expanded = lahanMenuExpanded,
                    onDismissRequest = { lahanMenuExpanded = false }
                ) {
                    allLahan.forEach { lahan ->
                        DropdownMenuItem(
                            text = {
                                Text("${lahan.nama} (${Formatters.formatInt(lahan.luas)} ${lahan.satuan})")
                            },
                            onClick = {
                                selectedLahanId = lahan.id
                                lahanMenuExpanded = false
                            },
                            modifier = Modifier.testTag("progres_lahan_option_${lahan.id}")
                        )
                    }
                }
            }

            // Stage Selector (6 steps, 3x2 grid)
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Pilih Tahapan Budidaya *",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val rows = StageType.entries.chunked(3)
                rows.forEach { rowStages ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowStages.forEach { stage ->
                            val isSelected = selectedStage == stage
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(
                                        if (isSelected) stage.badgeBg
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) stage.accentColor
                                        else MaterialTheme.colorScheme.outline,
                                        shape = RoundedCornerShape(14.dp)
                                    )
                                    .clickable {
                                        selectedStage = stage
                                        formError = null
                                    }
                                    .padding(horizontal = 8.dp, vertical = 10.dp)
                                    .testTag("stage_select_${stage.name.lowercase()}")
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = stage.icon(),
                                        contentDescription = null,
                                        tint = if (isSelected) stage.badgeContent else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${stage.step}. ${stage.shortLabel}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) stage.badgeContent else MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Tanggal Kegiatan
            OutlinedTextField(
                value = Formatters.formatDateIndo(tanggal),
                onValueChange = {},
                readOnly = true,
                label = { Text("Tanggal Kegiatan *") },
                trailingIcon = {
                    IconButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.testTag("progres_date_picker_btn")
                    ) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "Pilih tanggal kegiatan")
                    }
                },
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true }
                    .testTag("progres_tanggal_input")
            )

            // Dynamic Stage Inputs
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Detail ${selectedStage.fullLabel}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = selectedStage.badgeContent
                    )

                    when (selectedStage) {
                        StageType.BIBIT -> {
                            OutlinedTextField(
                                value = jumlahBibitText,
                                onValueChange = { jumlahBibitText = it; formError = null },
                                label = { Text("Jumlah Bibit *") },
                                placeholder = { Text("Contoh: 2000") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bibit_jumlah_input")
                            )
                            Text("Satuan Bibit", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Stek", "Batang", "Kg").forEach { opt ->
                                    FilterChip(
                                        selected = satuanBibit == opt,
                                        onClick = { satuanBibit = opt },
                                        label = { Text(opt) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald100,
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                            Text("Sumber Asal Bibit *", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Beli", "Stek Sendiri", "Bantuan").forEach { opt ->
                                    FilterChip(
                                        selected = sumberAsal == opt,
                                        onClick = { sumberAsal = opt },
                                        label = { Text(opt) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald100,
                                            selectedLabelColor = Emerald700
                                        ),
                                        modifier = Modifier.testTag("sumber_bibit_${opt.lowercase().replace(" ", "_")}")
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = detailSumber,
                                onValueChange = { detailSumber = it },
                                label = { Text("Detail Pemasok / Varietas / Harga") },
                                placeholder = { Text("Misal: Penangkar lokal Rp 350/stek, atau indukan Blok A") },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("bibit_detail_input")
                            )
                        }

                        StageType.TANAM -> {
                            OutlinedTextField(
                                value = jarakTanam,
                                onValueChange = { jarakTanam = it },
                                label = { Text("Jarak Tanam") },
                                placeholder = { Text("Contoh: 60 x 40 cm") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tanam_jarak_input")
                            )
                            Text("Metode Tanam", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Miring 45 Derajat", "Tegak Lurus", "Tidur / Rebah").forEach { opt ->
                                    FilterChip(
                                        selected = metodeTanam == opt,
                                        onClick = { metodeTanam = opt },
                                        label = { Text(opt) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald100,
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = tenagaKerjaText,
                                onValueChange = { tenagaKerjaText = it },
                                label = { Text("Jumlah Tenaga Kerja (Orang)") },
                                placeholder = { Text("Contoh: 3") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("tanam_tenaga_input")
                            )
                        }

                        StageType.PUPUK_AWAL -> {
                            OutlinedTextField(
                                value = jenisPupuk,
                                onValueChange = { jenisPupuk = it; formError = null },
                                label = { Text("Jenis Pupuk Dasar *") },
                                placeholder = { Text("Contoh: Pupuk Kandang Sapi + Dolomit") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pupuk_awal_jenis_input")
                            )
                            OutlinedTextField(
                                value = dosisKgText,
                                onValueChange = { dosisKgText = it; formError = null },
                                label = { Text("Dosis / Takaran (kg) *") },
                                placeholder = { Text("Contoh: 400") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pupuk_awal_dosis_input")
                            )
                            Text("Cara Aplikasi", style = MaterialTheme.typography.labelMedium)
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf("Tebar di Larikan Tanam", "Sebar di Bedengan", "Lubang Tanam").forEach { opt ->
                                    FilterChip(
                                        selected = caraAplikasi == opt,
                                        onClick = { caraAplikasi = opt },
                                        label = { Text(opt) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald100,
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                        }

                        StageType.PEMELIHARAAN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = umurHstText,
                                    onValueChange = { umurHstText = it },
                                    label = { Text("Umur (HST)") },
                                    placeholder = { Text("Misal: 28") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pemeliharaan_hst_input")
                                )
                                OutlinedTextField(
                                    value = tinggiCmText,
                                    onValueChange = { tinggiCmText = it; formError = null },
                                    label = { Text("Tinggi (cm) *") },
                                    placeholder = { Text("Misal: 85") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pemeliharaan_tinggi_input")
                                )
                            }
                            Text("Kegiatan Perawatan", style = MaterialTheme.typography.labelMedium)
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(
                                    "Kontrol Rutin",
                                    "Penyiangan Gulma",
                                    "Pembumbunan Tanah",
                                    "Penyiraman / Irigasi",
                                    "Penyulaman Bibit Mati"
                                ).forEach { opt ->
                                    FilterChip(
                                        selected = kegiatan == opt,
                                        onClick = { kegiatan = opt },
                                        label = { Text(opt) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = Emerald100,
                                            selectedLabelColor = Emerald700
                                        )
                                    )
                                }
                            }
                            OutlinedTextField(
                                value = kondisi,
                                onValueChange = { kondisi = it },
                                label = { Text("Kondisi Kesehatan Tanaman") },
                                placeholder = { Text("Contoh: Subur, daun hijau pekat, bebas gulma") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pemeliharaan_kondisi_input")
                            )
                        }

                        StageType.PUPUK_SUSULAN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = susulanKeText,
                                    onValueChange = { susulanKeText = it },
                                    label = { Text("Pemupukan Ke-") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pupuk_susulan_ke_input")
                                )
                                OutlinedTextField(
                                    value = dosisKgText,
                                    onValueChange = { dosisKgText = it; formError = null },
                                    label = { Text("Dosis (kg) *") },
                                    placeholder = { Text("Misal: 30") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("pupuk_susulan_dosis_input")
                                )
                            }
                            OutlinedTextField(
                                value = jenisPupuk,
                                onValueChange = { jenisPupuk = it; formError = null },
                                label = { Text("Jenis Pupuk Susulan *") },
                                placeholder = { Text("Contoh: Urea + NPK Phonska / POC") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pupuk_susulan_jenis_input")
                            )
                        }

                        StageType.PANEN -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedTextField(
                                    value = panenKeText,
                                    onValueChange = { panenKeText = it },
                                    label = { Text("Panen Ke-") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("panen_ke_input")
                                )
                                OutlinedTextField(
                                    value = umurPanenHariText,
                                    onValueChange = { umurPanenHariText = it },
                                    label = { Text("Umur (Hari)") },
                                    placeholder = { Text("Misal: 60") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("panen_umur_input")
                                )
                            }
                            OutlinedTextField(
                                value = hasilKgText,
                                onValueChange = { hasilKgText = it; formError = null },
                                label = { Text("Hasil Bobot Panen (kg) *") },
                                placeholder = { Text("Contoh: 3500") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("panen_hasil_input")
                            )
                            OutlinedTextField(
                                value = estimasiNilai,
                                onValueChange = { estimasiNilai = it },
                                label = { Text("Estimasi Nilai / Peruntukan") },
                                placeholder = { Text("Misal: Rp 2.450.000 atau Pakan Ternak Sendiri") },
                                singleLine = true,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("panen_estimasi_input")
                            )
                        }
                    }
                }
            }

            OutlinedTextField(
                value = catatan,
                onValueChange = { catatan = it },
                label = { Text("Catatan Lapangan") },
                placeholder = { Text("Kondisi cuaca, hama, atau pengamatan tambahan...") },
                minLines = 2,
                maxLines = 4,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("progres_catatan_input")
            )

            // Foto Progres Upload Card
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(18.dp))
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = selectedStage.photoTitle,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = selectedStage.photoHint,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val (uri, tempFile) = photoStorageManager.createCameraOutputUri()
                                pendingCameraTempFile = tempFile
                                takePictureLauncher.launch(uri)
                            },
                            enabled = !isProcessingPhoto,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_ambil_foto")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddAPhoto,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Ambil Foto")
                        }
                        OutlinedButton(
                            onClick = {
                                pickMultipleMediaLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            enabled = !isProcessingPhoto,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("btn_pilih_galeri")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
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
                            Text(
                                text = "Mengompresi & menyimpan foto...",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }

                    if (draftPhotos.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            draftPhotos.forEachIndexed { index, draft ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(MaterialTheme.colorScheme.surface)
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                    ) {
                                        ProgressPhotoImage(
                                            filePath = draft.filePath,
                                            contentDescription = "Preview foto ${index + 1}",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }
                                    OutlinedTextField(
                                        value = draft.caption,
                                        onValueChange = { newCap ->
                                            draftPhotos[index] = draft.copy(caption = newCap)
                                        },
                                        label = { Text("Keterangan Foto ${index + 1}") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("photo_caption_input_$index")
                                    )
                                    IconButton(
                                        onClick = {
                                            val removed = draftPhotos.removeAt(index)
                                            if (removed.isExisting) {
                                                removedExistingPaths.add(removed.filePath)
                                            } else {
                                                newlyCreatedPaths.remove(removed.filePath)
                                                scope.launch {
                                                    photoStorageManager.deleteLocalFiles(listOf(removed.filePath))
                                                }
                                            }
                                        },
                                        modifier = Modifier.testTag("remove_draft_photo_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Hapus foto ${index + 1}",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (formError != null) {
                Text(
                    text = formError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = dismissWithCleanup,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("cancel_progres_button")
                ) {
                    Text("Batal")
                }
                Button(
                    onClick = {
                        if (selectedLahanId == 0L) {
                            formError = "Pilih lahan terlebih dahulu"
                            return@Button
                        }
                        val parsedBibit = jumlahBibitText.replace(',', '.').toDoubleOrNull()
                        val parsedDosis = dosisKgText.replace(',', '.').toDoubleOrNull()
                        val parsedTinggi = tinggiCmText.replace(',', '.').toDoubleOrNull()
                        val parsedHasil = hasilKgText.replace(',', '.').toDoubleOrNull()

                        when (selectedStage) {
                            StageType.BIBIT -> {
                                if (parsedBibit == null || parsedBibit <= 0.0) {
                                    formError = "Masukkan jumlah bibit yang valid (> 0)"
                                    return@Button
                                }
                            }
                            StageType.PUPUK_AWAL, StageType.PUPUK_SUSULAN -> {
                                if (jenisPupuk.isBlank()) {
                                    formError = "Masukkan jenis pupuk yang digunakan"
                                    return@Button
                                }
                                if (parsedDosis == null || parsedDosis <= 0.0) {
                                    formError = "Masukkan dosis pupuk (kg) yang valid (> 0)"
                                    return@Button
                                }
                            }
                            StageType.PEMELIHARAAN -> {
                                if (parsedTinggi == null || parsedTinggi <= 0.0) {
                                    formError = "Masukkan tinggi tanaman (cm) untuk grafik pertumbuhan"
                                    return@Button
                                }
                            }
                            StageType.PANEN -> {
                                if (parsedHasil == null || parsedHasil <= 0.0) {
                                    formError = "Masukkan hasil bobot panen (kg) yang valid (> 0)"
                                    return@Button
                                }
                            }
                            StageType.TANAM -> Unit
                        }

                        val entity = ProgresEntity(
                            id = initialProgres?.id ?: 0L,
                            lahanId = selectedLahanId,
                            kategori = selectedStage.name,
                            tanggal = tanggal,
                            catatan = catatan.trim(),
                            jumlahBibit = if (selectedStage == StageType.BIBIT) parsedBibit else null,
                            satuanBibit = if (selectedStage == StageType.BIBIT) satuanBibit else null,
                            sumberAsal = if (selectedStage == StageType.BIBIT) sumberAsal else null,
                            detailSumber = if (selectedStage == StageType.BIBIT) detailSumber.trim() else null,
                            jarakTanam = if (selectedStage == StageType.TANAM) jarakTanam.trim() else null,
                            metodeTanam = if (selectedStage == StageType.TANAM) metodeTanam else null,
                            tenagaKerja = if (selectedStage == StageType.TANAM) tenagaKerjaText.toIntOrNull() else null,
                            jenisPupuk = if (selectedStage == StageType.PUPUK_AWAL || selectedStage == StageType.PUPUK_SUSULAN) jenisPupuk.trim() else null,
                            dosisKg = if (selectedStage == StageType.PUPUK_AWAL || selectedStage == StageType.PUPUK_SUSULAN) parsedDosis else null,
                            caraAplikasi = if (selectedStage == StageType.PUPUK_AWAL) caraAplikasi else null,
                            susulanKe = if (selectedStage == StageType.PUPUK_SUSULAN) (susulanKeText.toIntOrNull() ?: 1) else null,
                            umurHst = if (selectedStage == StageType.PEMELIHARAAN) umurHstText.toIntOrNull() else null,
                            tinggiCm = if (selectedStage == StageType.PEMELIHARAAN) parsedTinggi else null,
                            kegiatan = if (selectedStage == StageType.PEMELIHARAAN) kegiatan else null,
                            kondisi = if (selectedStage == StageType.PEMELIHARAAN) kondisi.trim() else null,
                            panenKe = if (selectedStage == StageType.PANEN) (panenKeText.toIntOrNull() ?: 1) else null,
                            hasilKg = if (selectedStage == StageType.PANEN) parsedHasil else null,
                            umurPanenHari = if (selectedStage == StageType.PANEN) umurPanenHariText.toIntOrNull() else null,
                            estimasiNilai = if (selectedStage == StageType.PANEN) estimasiNilai.trim() else null,
                            createdAt = initialProgres?.createdAt ?: System.currentTimeMillis()
                        )
                        newlyCreatedPaths.clear()
                        onSave(
                            entity,
                            draftPhotos.map { it.filePath to it.caption.trim() },
                            removedExistingPaths.toList()
                        )
                    },
                    enabled = !isProcessingPhoto,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("save_progres_button")
                ) {
                    Text(if (isEdit) "Simpan Perubahan" else "Simpan Progres")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        OdotDatePickerModal(
            initialIsoDate = tanggal,
            onDateSelected = { tanggal = it },
            onDismiss = { showDatePicker = false }
        )
    }
}

@Composable
fun PhotoLightboxDialog(
    state: LightboxState,
    onDismiss: () -> Unit
) {
    var currentIndex by remember(state) { mutableIntStateOf(state.initialIndex) }
    val safeIndex = currentIndex.coerceIn(0, state.photos.lastIndex)
    val (filePath, caption) = state.photos[safeIndex]

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        BackHandler(onBack = onDismiss)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            ProgressPhotoImage(
                filePath = filePath,
                contentDescription = caption.ifBlank { state.title },
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 72.dp)
            )

            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${state.subtitle} • Foto ${safeIndex + 1}/${state.photos.size}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("lightbox_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Tutup tampilan foto",
                        tint = Color.White
                    )
                }
            }

            // Prev / Next controls
            if (state.photos.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.Center)
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex = if (safeIndex == 0) state.photos.lastIndex else safeIndex - 1
                            },
                            modifier = Modifier.testTag("lightbox_prev_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Foto sebelumnya",
                                tint = Color.White
                            )
                        }
                    }
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        IconButton(
                            onClick = {
                                currentIndex = (safeIndex + 1) % state.photos.size
                            },
                            modifier = Modifier.testTag("lightbox_next_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Foto berikutnya",
                                tint = Color.White
                            )
                        }
                    }
                }
            }

            // Bottom caption & notes
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.72f))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (caption.isNotBlank()) {
                    Text(
                        text = caption,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                if (state.notes.isNotBlank()) {
                    Text(
                        text = state.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun OdotConfirmDialog(
    state: ConfirmDialogState,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    val (title, message, confirmText, isDestructive) = when (state) {
        is ConfirmDialogState.DeleteLahan -> listOf(
            "Hapus Data Lahan?",
            "Lahan \"${state.lahan.nama}\" beserta seluruh catatan progres dan fotonya akan dihapus permanen.",
            "Hapus Lahan",
            true
        )
        is ConfirmDialogState.DeleteProgres -> listOf(
            "Hapus Catatan Progres?",
            "Catatan tahapan ini beserta foto dokumentasinya akan dihapus dari riwayat lahan.",
            "Hapus Catatan",
            true
        )
        ConfirmDialogState.ResetSampleData -> listOf(
            "Muat Ulang Data Contoh?",
            "Seluruh data saat ini akan diganti dengan data simulasi awal Blok A & Blok B.",
            "Muat Ulang",
            false
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.WarningAmber,
                contentDescription = null,
                tint = if (isDestructive as Boolean) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )
        },
        title = {
            Text(text = title as String, fontWeight = FontWeight.Bold)
        },
        text = {
            Text(text = message as String)
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = if (isDestructive as Boolean) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                },
                modifier = Modifier.testTag("confirm_dialog_ok")
            ) {
                Text(confirmText as String)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("confirm_dialog_cancel")
            ) {
                Text("Batal")
            }
        }
    )
}
