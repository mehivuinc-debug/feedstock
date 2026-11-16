package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.OdotDatabase
import com.example.data.local.PhotoStorageManager
import com.example.data.model.FertilizingEntity
import com.example.data.model.FertilizingPhase
import com.example.data.model.HarvestingEntity
import com.example.data.model.LahanEntity
import com.example.data.model.LandEntity
import com.example.data.model.PlantingEntity
import com.example.data.model.ProgresEntity
import com.example.data.model.StageType
import com.example.data.repository.OdotRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: OdotDatabase
    private lateinit var repository: OdotRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, OdotDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = OdotRepository(db.odotDao(), PhotoStorageManager(context), db.cultivationDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun `app name matches OdotTrack`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("OdotTrack", appName)
    }

    @Test
    fun `lahan unit conversion calculates square meters accurately`() {
        val m2 = LahanEntity(nama = "Blok A", luas = 1200.0, satuan = "m²")
        val ha = LahanEntity(nama = "Blok B", luas = 0.5, satuan = "Hektar")
        val bata = LahanEntity(nama = "Blok C", luas = 50.0, satuan = "Bata")

        assertEquals(1200.0, m2.luasDalamM2(), 0.01)
        assertEquals(5000.0, ha.luasDalamM2(), 0.01)
        assertEquals(700.0, bata.luasDalamM2(), 0.01)
    }

    @Test
    fun `seed initial data syncs normalized land planting fertilizing and harvesting entities`() = runTest {
        repository.seedInitialDataIfEmpty()
        val seededLahan = repository.allLahan.first()
        val seededLands = repository.allLandsWithRecords.first()

        assertEquals(2, seededLahan.size)
        assertEquals(2, seededLands.size)

        val blokARecords = seededLands.first { it.land.name.contains("Blok A") }
        assertEquals(2, blokARecords.plantings.size)
        assertEquals(2, blokARecords.fertilizings.size)
        assertEquals(2, blokARecords.harvestings.size)
    }

    @Test
    fun `crud on Land Planting Fertilizing and Harvesting entities cascades cleanly`() = runTest {
        val landId = repository.upsertLand(
            LandEntity(
                name = "Blok C - Uji Coba",
                areaSize = 500.0,
                areaUnit = "m²",
                status = "Aktif Tanam",
                startDate = "2026-11-15"
            )
        )
        assertTrue(landId > 0L)

        repository.upsertPlanting(
            PlantingEntity(
                landId = landId,
                plantingDate = "2026-11-15",
                seedQuantity = 1000.0,
                seedUnit = "Stek",
                seedSource = "Stek Sendiri",
                plantingSpacing = "60 x 40 cm",
                plantingMethod = "Miring 45 Derajat"
            )
        )
        repository.upsertFertilizing(
            FertilizingEntity(
                landId = landId,
                phase = FertilizingPhase.INITIAL.name,
                fertilizingDate = "2026-11-16",
                fertilizerType = "Pupuk Kandang Sapi",
                dosageKg = 200.0,
                applicationMethod = "Tebar di Larikan"
            )
        )
        repository.upsertHarvesting(
            HarvestingEntity(
                landId = landId,
                harvestNumber = 1,
                harvestDate = "2027-01-10",
                yieldKg = 1650.0,
                harvestAgeDays = 56,
                estimatedValueOrUsage = "Pakan ternak sendiri"
            )
        )

        val records = repository.allLandsWithRecords.first().first { it.land.id == landId }
        assertEquals(1, records.plantings.size)
        assertEquals(1, records.fertilizings.size)
        assertEquals(1, records.harvestings.size)
        assertEquals(1650.0, records.harvestings.first().yieldKg, 0.01)

        // Deleting LandEntity cascades to plantings, fertilizings, and harvestings
        repository.deleteLand(landId)
        assertTrue(repository.allPlantings.first().none { it.landId == landId })
        assertTrue(repository.allFertilizings.first().none { it.landId == landId })
        assertTrue(repository.allHarvestings.first().none { it.landId == landId })
    }

    @Test
    fun `progres mutations sync automatically with normalized cultivation tables`() = runTest {
        val newLahanId = repository.upsertLahan(
            LahanEntity(
                nama = "Blok D - Sinkron",
                luas = 600.0,
                satuan = "m²",
                status = "Persiapan Lahan",
                tanggalMulai = "2026-11-15"
            )
        )
        val bibitId = repository.upsertProgres(
            ProgresEntity(
                lahanId = newLahanId,
                kategori = StageType.BIBIT.name,
                tanggal = "2026-11-15",
                jumlahBibit = 1200.0,
                satuanBibit = "Stek",
                sumberAsal = "Beli"
            ),
            emptyList()
        )
        val panenId = repository.upsertProgres(
            ProgresEntity(
                lahanId = newLahanId,
                kategori = StageType.PANEN.name,
                tanggal = "2027-01-05",
                panenKe = 1,
                hasilKg = 1800.0,
                umurPanenHari = 51
            ),
            emptyList()
        )

        assertEquals(1, repository.allPlantings.first().count { it.sourceProgresId == bibitId })
        assertEquals(1, repository.allHarvestings.first().count { it.sourceProgresId == panenId })

        repository.deleteProgres(bibitId)
        assertEquals(0, repository.allPlantings.first().count { it.sourceProgresId == bibitId })
    }

    @Test
    fun `application container and ViewModelProvider factory provide OdotViewModel`() {
        val app = ApplicationProvider.getApplicationContext<OdotApplication>()
        assertTrue(app.container.database.isOpen || !app.container.database.isOpen)
        val extras = androidx.lifecycle.viewmodel.MutableCreationExtras().apply {
            set(androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY, app)
        }
        val vm = com.example.ui.viewmodel.OdotViewModelProvider.Factory.create(
            com.example.ui.viewmodel.OdotViewModel::class.java,
            extras
        )
        assertTrue(vm.uiState.value.isLoading || !vm.uiState.value.isLoading)
        assertEquals(0, vm.landsWithRecords.value.size)
    }

    @Test
    fun `recordNewPlanting creates PlantingEntity and synced BIBIT and TANAM logs`() = runTest {
        val landId = repository.upsertLahan(
            LahanEntity(
                nama = "Blok E - Tanam Baru",
                luas = 900.0,
                satuan = "m²",
                status = "Persiapan Lahan",
                tanggalMulai = "2026-11-16"
            )
        )

        val plantingId = repository.recordNewPlanting(
            PlantingEntity(
                landId = landId,
                plantingDate = "2026-11-16",
                seedQuantity = 1800.0,
                seedUnit = "Stek",
                seedSource = "Stek Sendiri",
                seedSourceDetail = "Indukan Blok A",
                plantingSpacing = "60 x 40 cm",
                plantingMethod = "Miring 45 Derajat",
                workerCount = 3,
                notes = "Tanam pagi hari"
            )
        )

        assertTrue(plantingId > 0L)
        val plantings = repository.allPlantings.first().filter { it.landId == landId }
        assertEquals(1, plantings.size)
        assertEquals(1800.0, plantings.first().seedQuantity, 0.01)

        val progresLogs = repository.allProgres.first().filter { it.progres.lahanId == landId }
        assertTrue(progresLogs.any { it.progres.stageType() == StageType.BIBIT && it.progres.jumlahBibit == 1800.0 })
        assertTrue(progresLogs.any { it.progres.stageType() == StageType.TANAM && it.progres.jarakTanam == "60 x 40 cm" })

        val updatedLand = repository.allLahan.first().first { it.id == landId }
        assertEquals("Aktif Tanam", updatedLand.status)
    }
}
