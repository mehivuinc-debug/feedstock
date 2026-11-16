package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Landscape
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.PhotoStorageManager
import com.example.data.model.LahanEntity
import com.example.ui.components.LahanFormBottomSheet
import com.example.ui.components.OdotConfirmDialog
import com.example.ui.components.PhotoLightboxDialog
import com.example.ui.components.ProgresFormBottomSheet
import com.example.ui.screens.AddPlantingScreen
import com.example.ui.screens.BerandaScreen
import com.example.ui.screens.GaleriScreen
import com.example.ui.screens.GrafikScreen
import com.example.ui.screens.LahanScreen
import com.example.ui.theme.Emerald100
import com.example.ui.theme.Emerald700
import com.example.ui.theme.Emerald900
import com.example.ui.theme.OdotTrackTheme
import com.example.ui.viewmodel.AppTab
import com.example.ui.viewmodel.OdotViewModel
import com.example.ui.viewmodel.OdotViewModelProvider
import com.example.util.Formatters

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as OdotApplication).container

        setContent {
            OdotTrackTheme {
                val viewModel: OdotViewModel = viewModel(
                    factory = OdotViewModelProvider.Factory
                )
                OdotTrackApp(
                    viewModel = viewModel,
                    photoStorageManager = appContainer.photoStorageManager
                )
            }
        }
    }
}

private fun AppTab.icon(): ImageVector = when (this) {
    AppTab.BERANDA -> Icons.Default.Home
    AppTab.GRAFIK -> Icons.AutoMirrored.Filled.ShowChart
    AppTab.LAHAN -> Icons.Default.Landscape
    AppTab.GALERI -> Icons.Default.PhotoLibrary
}

@Composable
fun OdotTrackApp(
    viewModel: OdotViewModel,
    photoStorageManager: PhotoStorageManager
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val landsWithRecords by viewModel.landsWithRecords.collectAsStateWithLifecycle()
    val showAddPlantingScreen by viewModel.showAddPlantingScreen.collectAsStateWithLifecycle()
    val plantingPreselectedLandId by viewModel.plantingPreselectedLandId.collectAsStateWithLifecycle()
    val showLahanSheet by viewModel.showLahanSheet.collectAsStateWithLifecycle()
    val editingLahan by viewModel.editingLahan.collectAsStateWithLifecycle()
    val showProgresSheet by viewModel.showProgresSheet.collectAsStateWithLifecycle()
    val editingProgres by viewModel.editingProgres.collectAsStateWithLifecycle()
    val presetStage by viewModel.presetStage.collectAsStateWithLifecycle()
    val lightboxState by viewModel.lightboxState.collectAsStateWithLifecycle()
    val confirmDialog by viewModel.confirmDialog.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel) {
        viewModel.snackbarEvents.collect { msg ->
            snackbarHostState.showSnackbar(msg)
        }
    }

    if (showAddPlantingScreen) {
        AddPlantingScreen(
            allLahan = uiState.allLahan,
            preselectedLahanId = plantingPreselectedLandId,
            photoStorageManager = photoStorageManager,
            onNavigateBack = viewModel::closeAddPlantingScreen,
            onSavePlanting = viewModel::saveNewPlanting,
            onErrorMessage = viewModel::notifyMessage
        )
        return
    }

    BackHandler(enabled = uiState.currentTab != AppTab.BERANDA) {
        viewModel.selectTab(AppTab.BERANDA)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavRail = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = MaterialTheme.colorScheme.background,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                OdotTopHeader(
                    allLahan = uiState.allLahan,
                    activeLahanId = uiState.activeLahanId,
                    onSelectLahan = viewModel::selectActiveLahan,
                    onAddLahanClick = viewModel::openAddLahanSheet,
                    onResetSampleClick = viewModel::requestResetSampleData
                )
            },
            bottomBar = {
                if (!useNavRail) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 4.dp
                    ) {
                        AppTab.entries.forEach { tab ->
                            val selected = uiState.currentTab == tab
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(),
                                        contentDescription = tab.label
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.label,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Emerald700,
                                    selectedTextColor = Emerald700,
                                    indicatorColor = Emerald100
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }
            },
            floatingActionButton = {
                if (uiState.currentTab == AppTab.LAHAN) {
                    ExtendedFloatingActionButton(
                        onClick = viewModel::openAddLahanSheet,
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Tambah Lahan", fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .then(if (useNavRail) Modifier.navigationBarsPadding() else Modifier)
                            .testTag("fab_tambah_lahan")
                    )
                } else {
                    ExtendedFloatingActionButton(
                        onClick = { viewModel.openAddProgresSheet() },
                        icon = { Icon(Icons.Default.Add, contentDescription = null) },
                        text = { Text("Catat Progres", fontWeight = FontWeight.Bold) },
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .then(if (useNavRail) Modifier.navigationBarsPadding() else Modifier)
                            .testTag("fab_catat_progres")
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavRail) {
                    NavigationRail(
                        containerColor = MaterialTheme.colorScheme.surface,
                        modifier = Modifier
                            .fillMaxHeight()
                            .navigationBarsPadding()
                    ) {
                        Spacer(modifier = Modifier.height(12.dp))
                        AppTab.entries.forEach { tab ->
                            val selected = uiState.currentTab == tab
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.selectTab(tab) },
                                icon = {
                                    Icon(
                                        imageVector = tab.icon(),
                                        contentDescription = tab.label
                                    )
                                },
                                label = { Text(tab.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = Emerald700,
                                    selectedTextColor = Emerald700,
                                    indicatorColor = Emerald100
                                ),
                                modifier = Modifier.testTag("rail_tab_${tab.name.lowercase()}")
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else {
                        when (uiState.currentTab) {
                            AppTab.BERANDA -> BerandaScreen(
                                uiState = uiState,
                                onStageChecklistClick = { stage ->
                                    viewModel.openAddProgresSheet(presetStage = stage)
                                },
                                onStageFilterClick = viewModel::selectStageFilter,
                                onOpenFullCharts = { viewModel.selectTab(AppTab.GRAFIK) },
                                onAddProgresClick = { viewModel.openAddProgresSheet() },
                                onEditProgresClick = viewModel::openEditProgresSheet,
                                onDeleteProgresClick = viewModel::requestDeleteProgres,
                                onOpenLightbox = { item, idx ->
                                    val lahanName = uiState.allLahan
                                        .firstOrNull { it.id == item.progres.lahanId }?.nama ?: "Lahan"
                                    val stage = item.progres.stageType()
                                    viewModel.openLightbox(
                                        title = "${stage.step}. ${stage.fullLabel}",
                                        subtitle = "$lahanName • ${Formatters.formatDateIndo(item.progres.tanggal)}",
                                        notes = item.progres.catatan,
                                        photos = item.fotos.map { it.filePath to it.caption },
                                        initialIndex = idx
                                    )
                                }
                            )

                            AppTab.GRAFIK -> GrafikScreen(
                                uiState = uiState,
                                onInspectLahanTimeline = viewModel::selectLahanAndShowBeranda
                            )

                            AppTab.LAHAN -> LahanScreen(
                                uiState = uiState,
                                landsWithRecords = landsWithRecords,
                                onAddLahanClick = viewModel::openAddLahanSheet,
                                onEditLahanClick = viewModel::openEditLahanSheet,
                                onDeleteLahanClick = viewModel::requestDeleteLahan,
                                onAddPlantingForLahan = viewModel::openAddPlantingScreen,
                                onShowTimeline = viewModel::selectLahanAndShowBeranda,
                                onShowGrafik = viewModel::selectLahanAndShowGrafik
                            )

                            AppTab.GALERI -> GaleriScreen(
                                uiState = uiState,
                                onStageFilterClick = viewModel::selectGalleryStageFilter,
                                onAddPhotoClick = { viewModel.openAddProgresSheet() },
                                onPhotoClick = { photo ->
                                    val matchingProgres = uiState.allProgres
                                        .firstOrNull { it.progres.id == photo.progresId }
                                    if (matchingProgres != null && matchingProgres.fotos.isNotEmpty()) {
                                        viewModel.openLightbox(
                                            title = "${photo.stageType.step}. ${photo.stageType.fullLabel}",
                                            subtitle = "${photo.lahanNama} • ${Formatters.formatDateIndo(photo.tanggal)}",
                                            notes = photo.catatan,
                                            photos = matchingProgres.fotos.map { it.filePath to it.caption },
                                            initialIndex = photo.photoIndex
                                        )
                                    } else {
                                        viewModel.openLightbox(
                                            title = "${photo.stageType.step}. ${photo.stageType.fullLabel}",
                                            subtitle = "${photo.lahanNama} • ${Formatters.formatDateIndo(photo.tanggal)}",
                                            notes = photo.catatan,
                                            photos = listOf(photo.filePath to photo.caption),
                                            initialIndex = 0
                                        )
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    if (showLahanSheet) {
        LahanFormBottomSheet(
            editingLahan = editingLahan,
            onDismiss = viewModel::closeLahanSheet,
            onSave = viewModel::saveLahan
        )
    }

    if (showProgresSheet) {
        ProgresFormBottomSheet(
            allLahan = uiState.allLahan,
            activeLahanId = uiState.activeLahanId,
            editingProgres = editingProgres,
            presetStage = presetStage,
            photoStorageManager = photoStorageManager,
            onDismiss = viewModel::closeProgresSheet,
            onSave = viewModel::saveProgres,
            onErrorMessage = viewModel::notifyMessage
        )
    }

    lightboxState?.let { lbState ->
        PhotoLightboxDialog(
            state = lbState,
            onDismiss = viewModel::closeLightbox
        )
    }

    confirmDialog?.let { dialogState ->
        OdotConfirmDialog(
            state = dialogState,
            onDismiss = viewModel::dismissConfirmDialog,
            onConfirm = viewModel::confirmPendingAction
        )
    }
}

@Composable
private fun OdotTopHeader(
    allLahan: List<LahanEntity>,
    activeLahanId: Long?,
    onSelectLahan: (Long?) -> Unit,
    onAddLahanClick: () -> Unit,
    onResetSampleClick: () -> Unit
) {
    var selectorExpanded by remember { mutableStateOf(false) }
    val activeLabel = activeLahanId?.let { id ->
        allLahan.firstOrNull { it.id == id }?.nama
    } ?: "Semua Lahan (${allLahan.size} Blok)"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.linearGradient(
                    colors = listOf(Emerald900, Emerald700)
                )
            )
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White.copy(alpha = 0.18f),
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Eco,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "OdotTrack",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "Monitor Budidaya Rumput Odot",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                IconButton(
                    onClick = onResetSampleClick,
                    modifier = Modifier.testTag("reset_sample_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = "Muat ulang data contoh",
                        tint = Color.White
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color.White.copy(alpha = 0.16f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectorExpanded = true }
                            .testTag("active_lahan_selector")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Landscape,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeLabel,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Pilih lahan aktif",
                                tint = Color.White
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = selectorExpanded,
                        onDismissRequest = { selectorExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Semua Lahan (${allLahan.size} Blok)") },
                            trailingIcon = {
                                if (activeLahanId == null) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Emerald700)
                                }
                            },
                            onClick = {
                                onSelectLahan(null)
                                selectorExpanded = false
                            },
                            modifier = Modifier.testTag("selector_option_all")
                        )
                        allLahan.forEach { lahan ->
                            DropdownMenuItem(
                                text = {
                                    Text("${lahan.nama} (${Formatters.formatInt(lahan.luas)} ${lahan.satuan})")
                                },
                                trailingIcon = {
                                    if (activeLahanId == lahan.id) {
                                        Icon(Icons.Default.Check, contentDescription = null, tint = Emerald700)
                                    }
                                },
                                onClick = {
                                    onSelectLahan(lahan.id)
                                    selectorExpanded = false
                                },
                                modifier = Modifier.testTag("selector_option_${lahan.id}")
                            )
                        }
                    }
                }

                FilledTonalButton(
                    onClick = onAddLahanClick,
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = Color.White,
                        contentColor = Emerald900
                    ),
                    modifier = Modifier
                        .height(48.dp)
                        .testTag("header_add_lahan_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Lahan", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
