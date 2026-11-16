package com.example.ui.viewmodel

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.LahanEntity
import com.example.data.model.LandWithCultivationRecords
import com.example.data.model.PlantingEntity
import com.example.data.model.ProgresEntity
import com.example.data.model.ProgresWithPhotos
import com.example.data.model.StageType
import com.example.data.repository.OdotRepository
import com.example.ui.theme.Emerald600
import com.example.ui.theme.Orange600
import com.example.ui.theme.Purple700
import com.example.ui.theme.Sky700
import com.example.ui.theme.Teal700
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class AppTab(val label: String) {
    BERANDA("Beranda"),
    GRAFIK("Grafik"),
    LAHAN("Data Lahan"),
    GALERI("Galeri")
}

data class GrowthPoint(
    val progresId: Long,
    val lahanId: Long,
    val lahanNama: String,
    val tanggal: String,
    val umurHst: Int?,
    val tinggiCm: Double
)

data class LahanGrowthSeries(
    val lahanId: Long,
    val lahanNama: String,
    val color: Color,
    val points: List<GrowthPoint>
)

data class LahanProductivityMetric(
    val lahan: LahanEntity,
    val luasM2: Double,
    val totalBibit: Double,
    val totalPupukKg: Double,
    val totalPanenKg: Double,
    val harvestCount: Int,
    val efficiencyKgPerM2: Double,
    val kgPerBibit: Double,
    val latestHeightCm: Double?,
    val growthRateCmPerDay: Double?,
    val logCount: Int,
    val photoCount: Int,
    val completedStagesCount: Int,
    val seriesColor: Color
)

data class HarvestRecordPoint(
    val progresId: Long,
    val lahanId: Long,
    val lahanNama: String,
    val panenKe: Int,
    val tanggal: String,
    val hasilKg: Double,
    val umurHari: Int?
)

data class GalleryPhotoItem(
    val progresId: Long,
    val photoIndex: Int,
    val filePath: String,
    val caption: String,
    val stageType: StageType,
    val lahanNama: String,
    val tanggal: String,
    val catatan: String
)

data class LightboxState(
    val title: String,
    val subtitle: String,
    val notes: String,
    val photos: List<Pair<String, String>>,
    val initialIndex: Int
)

sealed interface ConfirmDialogState {
    data class DeleteLahan(val lahan: LahanEntity) : ConfirmDialogState
    data class DeleteProgres(val progresId: Long) : ConfirmDialogState
    data object ResetSampleData : ConfirmDialogState
}

data class OdotUiState(
    val isLoading: Boolean = true,
    val allLahan: List<LahanEntity> = emptyList(),
    val allProgres: List<ProgresWithPhotos> = emptyList(),
    val activeLahanId: Long? = null,
    val activeStageFilter: StageType? = null,
    val activeGalleryStageFilter: StageType? = null,
    val currentTab: AppTab = AppTab.BERANDA,
    // Derived filtered data
    val filteredLahan: List<LahanEntity> = emptyList(),
    val filteredProgresForTimeline: List<ProgresWithPhotos> = emptyList(),
    val filteredStageMap: Map<StageType, List<ProgresWithPhotos>> = emptyMap(),
    val galleryPhotos: List<GalleryPhotoItem> = emptyList(),
    // KPI & Real-Time Productivity Analytics
    val totalLuasM2: Double = 0.0,
    val totalBibit: Double = 0.0,
    val totalPanenKg: Double = 0.0,
    val totalPupukKg: Double = 0.0,
    val avgProductivityKgPerM2: Double = 0.0,
    val avgCycleDays: Double = 0.0,
    val avgGrowthRateCmPerDay: Double = 0.0,
    val lahanMetrics: List<LahanProductivityMetric> = emptyList(),
    val growthSeries: List<LahanGrowthSeries> = emptyList(),
    val harvestHistory: List<HarvestRecordPoint> = emptyList()
)

class OdotViewModel(
    private val repository: OdotRepository
) : ViewModel() {

    private val _activeLahanId = MutableStateFlow<Long?>(null)
    private val _activeStageFilter = MutableStateFlow<StageType?>(null)
    private val _activeGalleryStageFilter = MutableStateFlow<StageType?>(null)
    private val _currentTab = MutableStateFlow(AppTab.BERANDA)

    private val _showLahanSheet = MutableStateFlow(false)
    val showLahanSheet: StateFlow<Boolean> = _showLahanSheet.asStateFlow()

    private val _editingLahan = MutableStateFlow<LahanEntity?>(null)
    val editingLahan: StateFlow<LahanEntity?> = _editingLahan.asStateFlow()

    private val _showProgresSheet = MutableStateFlow(false)
    val showProgresSheet: StateFlow<Boolean> = _showProgresSheet.asStateFlow()

    private val _editingProgres = MutableStateFlow<ProgresWithPhotos?>(null)
    val editingProgres: StateFlow<ProgresWithPhotos?> = _editingProgres.asStateFlow()

    private val _presetStage = MutableStateFlow(StageType.BIBIT)
    val presetStage: StateFlow<StageType> = _presetStage.asStateFlow()

    private val _lightboxState = MutableStateFlow<LightboxState?>(null)
    val lightboxState: StateFlow<LightboxState?> = _lightboxState.asStateFlow()

    private val _confirmDialog = MutableStateFlow<ConfirmDialogState?>(null)
    val confirmDialog: StateFlow<ConfirmDialogState?> = _confirmDialog.asStateFlow()

    private val _showAddPlantingScreen = MutableStateFlow(false)
    val showAddPlantingScreen: StateFlow<Boolean> = _showAddPlantingScreen.asStateFlow()

    private val _plantingPreselectedLandId = MutableStateFlow<Long?>(null)
    val plantingPreselectedLandId: StateFlow<Long?> = _plantingPreselectedLandId.asStateFlow()

    private val _snackbarEvents = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val snackbarEvents: SharedFlow<String> = _snackbarEvents.asSharedFlow()

    private val seriesPalette = listOf(
        Emerald600,
        Orange600,
        Sky700,
        Purple700,
        Teal700
    )

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
        }
    }

    val uiState: StateFlow<OdotUiState> = combine(
        repository.allLahan,
        repository.allProgres,
        _activeLahanId,
        _activeStageFilter,
        combine(_activeGalleryStageFilter, _currentTab) { g, t -> g to t }
    ) { lahanList, progresList, activeLahanId, stageFilter, (galleryFilter, currentTab) ->
        val validActiveLahanId = if (activeLahanId != null && lahanList.any { it.id == activeLahanId }) {
            activeLahanId
        } else {
            null
        }

        val lahanLookup = lahanList.associateBy { it.id }

        val filteredLahan = if (validActiveLahanId == null) {
            lahanList
        } else {
            lahanList.filter { it.id == validActiveLahanId }
        }

        val progresByActiveLahan = if (validActiveLahanId == null) {
            progresList
        } else {
            progresList.filter { it.progres.lahanId == validActiveLahanId }
        }

        val timelineProgres = if (stageFilter == null) {
            progresByActiveLahan
        } else {
            progresByActiveLahan.filter { it.progres.stageType() == stageFilter }
        }

        val stageMap = StageType.entries.associateWith { stage ->
            progresByActiveLahan.filter { it.progres.stageType() == stage }
        }

        val galleryItems = progresByActiveLahan
            .filter { galleryFilter == null || it.progres.stageType() == galleryFilter }
            .flatMap { item ->
                val lahanNama = lahanLookup[item.progres.lahanId]?.nama ?: "Lahan"
                item.fotos.mapIndexed { idx, foto ->
                    GalleryPhotoItem(
                        progresId = item.progres.id,
                        photoIndex = idx,
                        filePath = foto.filePath,
                        caption = foto.caption,
                        stageType = item.progres.stageType(),
                        lahanNama = lahanNama,
                        tanggal = item.progres.tanggal,
                        catatan = item.progres.catatan
                    )
                }
            }

        // Per-Lahan Metrics (always computed for all lahan so comparison charts work smoothly)
        val allLahanMetrics = lahanList.mapIndexed { idx, lahan ->
            val logs = progresList.filter { it.progres.lahanId == lahan.id }
            val luasM2 = lahan.luasDalamM2().coerceAtLeast(1.0)
            val totalBibit = logs
                .filter { it.progres.stageType() == StageType.BIBIT }
                .sumOf { it.progres.jumlahBibit ?: 0.0 }
            val totalPupuk = logs
                .filter {
                    it.progres.stageType() == StageType.PUPUK_AWAL ||
                        it.progres.stageType() == StageType.PUPUK_SUSULAN
                }
                .sumOf { it.progres.dosisKg ?: 0.0 }
            val harvestLogs = logs.filter { it.progres.stageType() == StageType.PANEN }
            val totalPanen = harvestLogs.sumOf { it.progres.hasilKg ?: 0.0 }
            val heightLogs = logs
                .filter { (it.progres.tinggiCm ?: 0.0) > 0.0 }
                .sortedBy { it.progres.tanggal }
            val latestHeight = heightLogs.lastOrNull()?.progres?.tinggiCm
            val growthRates = heightLogs.mapNotNull { log ->
                val h = log.progres.tinggiCm ?: return@mapNotNull null
                val hst = log.progres.umurHst ?: return@mapNotNull null
                if (hst > 0) h / hst.toDouble() else null
            }
            val avgGrowthRate = if (growthRates.isNotEmpty()) growthRates.average() else null
            val completedStages = logs.map { it.progres.stageType() }.distinct().size
            val photoCount = logs.sumOf { it.fotos.size }

            LahanProductivityMetric(
                lahan = lahan,
                luasM2 = luasM2,
                totalBibit = totalBibit,
                totalPupukKg = totalPupuk,
                totalPanenKg = totalPanen,
                harvestCount = harvestLogs.size,
                efficiencyKgPerM2 = totalPanen / luasM2,
                kgPerBibit = if (totalBibit > 0) totalPanen / totalBibit else 0.0,
                latestHeightCm = latestHeight,
                growthRateCmPerDay = avgGrowthRate,
                logCount = logs.size,
                photoCount = photoCount,
                completedStagesCount = completedStages,
                seriesColor = seriesPalette[idx % seriesPalette.size]
            )
        }

        val totalLuasM2 = filteredLahan.sumOf { it.luasDalamM2() }
        val totalBibit = progresByActiveLahan
            .filter { it.progres.stageType() == StageType.BIBIT }
            .sumOf { it.progres.jumlahBibit ?: 0.0 }
        val harvestLogsFiltered = progresByActiveLahan.filter { it.progres.stageType() == StageType.PANEN }
        val totalPanenKg = harvestLogsFiltered.sumOf { it.progres.hasilKg ?: 0.0 }
        val totalPupukKg = progresByActiveLahan
            .filter {
                it.progres.stageType() == StageType.PUPUK_AWAL ||
                    it.progres.stageType() == StageType.PUPUK_SUSULAN
            }
            .sumOf { it.progres.dosisKg ?: 0.0 }

        val avgEfficiency = if (totalLuasM2 > 0) totalPanenKg / totalLuasM2 else 0.0
        val cycleDaysList = harvestLogsFiltered.mapNotNull { it.progres.umurPanenHari?.takeIf { d -> d > 0 } }
        val avgCycleDays = if (cycleDaysList.isNotEmpty()) cycleDaysList.average() else 0.0

        val growthRatesFiltered = progresByActiveLahan.mapNotNull { item ->
            val h = item.progres.tinggiCm ?: return@mapNotNull null
            val hst = item.progres.umurHst ?: return@mapNotNull null
            if (h > 0 && hst > 0) h / hst.toDouble() else null
        }
        val avgGrowthRate = if (growthRatesFiltered.isNotEmpty()) growthRatesFiltered.average() else 0.0

        // Growth Series for Line Chart
        val metricsColorMap = allLahanMetrics.associate { it.lahan.id to it.seriesColor }
        val growthSeries = filteredLahan.mapNotNull { lahan ->
            val points = progresList
                .filter { it.progres.lahanId == lahan.id && (it.progres.tinggiCm ?: 0.0) > 0.0 }
                .sortedBy { it.progres.tanggal }
                .map { item ->
                    GrowthPoint(
                        progresId = item.progres.id,
                        lahanId = lahan.id,
                        lahanNama = lahan.nama,
                        tanggal = item.progres.tanggal,
                        umurHst = item.progres.umurHst,
                        tinggiCm = item.progres.tinggiCm ?: 0.0
                    )
                }
            if (points.isEmpty()) null
            else LahanGrowthSeries(
                lahanId = lahan.id,
                lahanNama = lahan.nama,
                color = metricsColorMap[lahan.id] ?: Emerald600,
                points = points
            )
        }

        val harvestHistory = harvestLogsFiltered
            .sortedBy { it.progres.tanggal }
            .map { item ->
                HarvestRecordPoint(
                    progresId = item.progres.id,
                    lahanId = item.progres.lahanId,
                    lahanNama = lahanLookup[item.progres.lahanId]?.nama ?: "Lahan",
                    panenKe = item.progres.panenKe ?: 1,
                    tanggal = item.progres.tanggal,
                    hasilKg = item.progres.hasilKg ?: 0.0,
                    umurHari = item.progres.umurPanenHari
                )
            }

        OdotUiState(
            isLoading = false,
            allLahan = lahanList,
            allProgres = progresList,
            activeLahanId = validActiveLahanId,
            activeStageFilter = stageFilter,
            activeGalleryStageFilter = galleryFilter,
            currentTab = currentTab,
            filteredLahan = filteredLahan,
            filteredProgresForTimeline = timelineProgres,
            filteredStageMap = stageMap,
            galleryPhotos = galleryItems,
            totalLuasM2 = totalLuasM2,
            totalBibit = totalBibit,
            totalPanenKg = totalPanenKg,
            totalPupukKg = totalPupukKg,
            avgProductivityKgPerM2 = avgEfficiency,
            avgCycleDays = avgCycleDays,
            avgGrowthRateCmPerDay = avgGrowthRate,
            lahanMetrics = allLahanMetrics,
            growthSeries = growthSeries,
            harvestHistory = harvestHistory
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = OdotUiState()
    )

    val landsWithRecords: StateFlow<List<LandWithCultivationRecords>> =
        repository.allLandsWithRecords.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = emptyList()
        )

    fun selectTab(tab: AppTab) {
        _currentTab.value = tab
    }

    fun selectActiveLahan(lahanId: Long?) {
        _activeLahanId.value = lahanId
    }

    fun selectLahanAndShowBeranda(lahanId: Long) {
        _activeLahanId.value = lahanId
        _currentTab.value = AppTab.BERANDA
    }

    fun selectLahanAndShowGrafik(lahanId: Long) {
        _activeLahanId.value = lahanId
        _currentTab.value = AppTab.GRAFIK
    }

    fun selectStageFilter(stage: StageType?) {
        _activeStageFilter.value = stage
    }

    fun selectGalleryStageFilter(stage: StageType?) {
        _activeGalleryStageFilter.value = stage
    }

    fun openAddLahanSheet() {
        _editingLahan.value = null
        _showLahanSheet.value = true
    }

    fun openEditLahanSheet(lahan: LahanEntity) {
        _editingLahan.value = lahan
        _showLahanSheet.value = true
    }

    fun closeLahanSheet() {
        _showLahanSheet.value = false
        _editingLahan.value = null
    }

    fun saveLahan(lahan: LahanEntity) {
        viewModelScope.launch {
            val isNew = lahan.id == 0L
            val savedId = repository.upsertLahan(lahan)
            if (isNew) {
                _activeLahanId.value = savedId
            }
            closeLahanSheet()
            _snackbarEvents.tryEmit(
                if (isNew) "Lahan baru berhasil ditambahkan" else "Data lahan diperbarui"
            )
        }
    }

    fun openAddProgresSheet(presetStage: StageType = StageType.BIBIT) {
        if (uiState.value.allLahan.isEmpty()) {
            _snackbarEvents.tryEmit("Tambahkan data lahan terlebih dahulu!")
            openAddLahanSheet()
            return
        }
        _editingProgres.value = null
        _presetStage.value = presetStage
        _showProgresSheet.value = true
    }

    fun openEditProgresSheet(progres: ProgresWithPhotos) {
        _editingProgres.value = progres
        _presetStage.value = progres.progres.stageType()
        _showProgresSheet.value = true
    }

    fun closeProgresSheet() {
        _showProgresSheet.value = false
        _editingProgres.value = null
    }

    fun saveProgres(
        progres: ProgresEntity,
        photos: List<Pair<String, String>>,
        removedPaths: List<String>
    ) {
        viewModelScope.launch {
            val isNew = progres.id == 0L
            repository.upsertProgres(progres, photos, removedPaths)
            closeProgresSheet()
            val label = progres.stageType().shortLabel
            _snackbarEvents.tryEmit(
                if (isNew) "Progres $label berhasil disimpan" else "Catatan $label diperbarui"
            )
        }
    }

    fun openAddPlantingScreen(lahanId: Long? = null) {
        if (uiState.value.allLahan.isEmpty()) {
            _snackbarEvents.tryEmit("Tambahkan data lahan terlebih dahulu!")
            openAddLahanSheet()
            return
        }
        _plantingPreselectedLandId.value = lahanId ?: _activeLahanId.value ?: uiState.value.allLahan.firstOrNull()?.id
        _showAddPlantingScreen.value = true
    }

    fun closeAddPlantingScreen() {
        _showAddPlantingScreen.value = false
        _plantingPreselectedLandId.value = null
    }

    fun saveNewPlanting(
        planting: PlantingEntity,
        photos: List<Pair<String, String>>
    ) {
        viewModelScope.launch {
            repository.recordNewPlanting(planting, photos)
            closeAddPlantingScreen()
            _snackbarEvents.tryEmit("Data penanaman baru berhasil disimpan")
        }
    }

    fun requestDeleteLahan(lahan: LahanEntity) {
        _confirmDialog.value = ConfirmDialogState.DeleteLahan(lahan)
    }

    fun requestDeleteProgres(progresId: Long) {
        _confirmDialog.value = ConfirmDialogState.DeleteProgres(progresId)
    }

    fun requestResetSampleData() {
        _confirmDialog.value = ConfirmDialogState.ResetSampleData
    }

    fun dismissConfirmDialog() {
        _confirmDialog.value = null
    }

    fun confirmPendingAction() {
        val action = _confirmDialog.value ?: return
        _confirmDialog.value = null
        viewModelScope.launch {
            when (action) {
                is ConfirmDialogState.DeleteLahan -> {
                    repository.deleteLahan(action.lahan.id)
                    if (_activeLahanId.value == action.lahan.id) {
                        _activeLahanId.value = null
                    }
                    _snackbarEvents.tryEmit("Lahan & riwayatnya dihapus")
                }
                is ConfirmDialogState.DeleteProgres -> {
                    repository.deleteProgres(action.progresId)
                    _snackbarEvents.tryEmit("Catatan progres dihapus")
                }
                ConfirmDialogState.ResetSampleData -> {
                    _activeLahanId.value = null
                    _activeStageFilter.value = null
                    _activeGalleryStageFilter.value = null
                    repository.resetToSampleData()
                    _snackbarEvents.tryEmit("Data contoh berhasil dimuat ulang")
                }
            }
        }
    }

    fun openLightbox(
        title: String,
        subtitle: String,
        notes: String,
        photos: List<Pair<String, String>>,
        initialIndex: Int = 0
    ) {
        if (photos.isEmpty()) return
        _lightboxState.value = LightboxState(
            title = title,
            subtitle = subtitle,
            notes = notes,
            photos = photos,
            initialIndex = initialIndex.coerceIn(0, photos.lastIndex)
        )
    }

    fun closeLightbox() {
        _lightboxState.value = null
    }

    fun notifyMessage(msg: String) {
        _snackbarEvents.tryEmit(msg)
    }

    class Factory(private val repository: OdotRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OdotViewModel::class.java)) {
                return OdotViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}
