package com.example.data.repository

import com.example.data.local.CultivationDao
import com.example.data.local.OdotDao
import com.example.data.local.PhotoStorageManager
import com.example.data.model.FertilizingEntity
import com.example.data.model.FertilizingPhase
import com.example.data.model.HarvestingEntity
import com.example.data.model.LahanEntity
import com.example.data.model.LandEntity
import com.example.data.model.LandWithCultivationRecords
import com.example.data.model.PlantingEntity
import com.example.data.model.ProgresEntity
import com.example.data.model.ProgresWithPhotos
import com.example.data.model.StageType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class OdotRepository(
    private val dao: OdotDao,
    private val photoStorageManager: PhotoStorageManager,
    private val cultivationDao: CultivationDao? = null
) {
    val allLahan: Flow<List<LahanEntity>> = dao.observeAllLahan()
    val allProgres: Flow<List<ProgresWithPhotos>> = dao.observeAllProgresWithPhotos()

    val allLands: Flow<List<LandEntity>> = cultivationDao?.observeAllLands() ?: emptyFlow()
    val allLandsWithRecords: Flow<List<LandWithCultivationRecords>> =
        cultivationDao?.observeAllLandsWithRecords() ?: emptyFlow()
    val allPlantings: Flow<List<PlantingEntity>> = cultivationDao?.observeAllPlantings() ?: emptyFlow()
    val allFertilizings: Flow<List<FertilizingEntity>> = cultivationDao?.observeAllFertilizings() ?: emptyFlow()
    val allHarvestings: Flow<List<HarvestingEntity>> = cultivationDao?.observeAllHarvestings() ?: emptyFlow()

    suspend fun seedInitialDataIfEmpty() {
        if (dao.countLahan() == 0) {
            insertSampleData()
        }
    }

    suspend fun resetToSampleData() {
        val existingFiles = dao.getAllPhotoPaths()
        dao.deleteAllLahan()
        cultivationDao?.deleteAllLands()
        photoStorageManager.deleteLocalFiles(existingFiles)
        insertSampleData()
    }

    suspend fun upsertLahan(lahan: LahanEntity): Long {
        val savedId = if (lahan.id == 0L) {
            dao.insertLahan(lahan)
        } else {
            dao.updateLahan(lahan)
            lahan.id
        }
        syncLandFromLahan(lahan.copy(id = savedId))
        return savedId
    }

    suspend fun deleteLahan(lahanId: Long) {
        val paths = dao.getPhotoPathsForLahan(lahanId)
        dao.deleteLahanById(lahanId)
        cultivationDao?.deleteLandById(lahanId)
        photoStorageManager.deleteLocalFiles(paths)
    }

    suspend fun upsertProgres(
        progres: ProgresEntity,
        photos: List<Pair<String, String>>,
        removedPaths: List<String> = emptyList()
    ): Long {
        val isCreate = progres.id == 0L
        val savedId = dao.upsertProgresWithPhotos(progres, photos)
        syncCultivationRecordFromProgres(progres.copy(id = savedId))
        if (removedPaths.isNotEmpty()) {
            photoStorageManager.deleteLocalFiles(removedPaths)
        }
        if (isCreate) {
            when (progres.stageType()) {
                StageType.TANAM -> dao.updateLahanStatus(progres.lahanId, "Aktif Tanam")
                StageType.PANEN -> dao.updateLahanStatus(progres.lahanId, "Istirahat / Pasca Panen")
                else -> Unit
            }
        }
        return savedId
    }

    suspend fun deleteProgres(progresId: Long) {
        val paths = dao.getPhotoPathsForProgres(progresId)
        dao.deleteProgresById(progresId)
        cultivationDao?.deletePlantingBySourceProgresId(progresId)
        cultivationDao?.deleteFertilizingBySourceProgresId(progresId)
        cultivationDao?.deleteHarvestingBySourceProgresId(progresId)
        photoStorageManager.deleteLocalFiles(paths)
    }

    // Direct normalized entity CRUD helpers
    suspend fun upsertLand(land: LandEntity): Long {
        val cDao = cultivationDao ?: return 0L
        return if (land.id == 0L) cDao.insertLand(land) else {
            cDao.updateLand(land.copy(updatedAt = System.currentTimeMillis()))
            land.id
        }
    }

    suspend fun deleteLand(landId: Long) {
        cultivationDao?.deleteLandById(landId)
    }

    suspend fun upsertPlanting(planting: PlantingEntity): Long {
        val cDao = cultivationDao ?: return 0L
        return if (planting.id == 0L) cDao.insertPlanting(planting) else {
            cDao.updatePlanting(planting)
            planting.id
        }
    }

    suspend fun recordNewPlanting(
        planting: PlantingEntity,
        photos: List<Pair<String, String>> = emptyList()
    ): Long {
        if (planting.seedQuantity > 0.0) {
            dao.upsertProgresWithPhotos(
                ProgresEntity(
                    lahanId = planting.landId,
                    kategori = StageType.BIBIT.name,
                    tanggal = planting.plantingDate,
                    jumlahBibit = planting.seedQuantity,
                    satuanBibit = planting.seedUnit,
                    sumberAsal = planting.seedSource,
                    detailSumber = planting.seedSourceDetail,
                    catatan = planting.notes
                ),
                emptyList()
            )
        }

        val tanamProgresId = dao.upsertProgresWithPhotos(
            ProgresEntity(
                lahanId = planting.landId,
                kategori = StageType.TANAM.name,
                tanggal = planting.plantingDate,
                jarakTanam = planting.plantingSpacing,
                metodeTanam = planting.plantingMethod,
                tenagaKerja = planting.workerCount,
                catatan = planting.notes
            ),
            photos
        )

        dao.updateLahanStatus(planting.landId, "Aktif Tanam")

        val cDao = cultivationDao ?: return tanamProgresId
        val syncedLandId = planting.landId
        return cDao.insertPlanting(
            planting.copy(
                landId = syncedLandId,
                sourceProgresId = tanamProgresId
            )
        )
    }

    suspend fun deletePlanting(plantingId: Long) {
        cultivationDao?.deletePlantingById(plantingId)
    }

    suspend fun upsertFertilizing(fertilizing: FertilizingEntity): Long {
        val cDao = cultivationDao ?: return 0L
        return if (fertilizing.id == 0L) cDao.insertFertilizing(fertilizing) else {
            cDao.updateFertilizing(fertilizing)
            fertilizing.id
        }
    }

    suspend fun deleteFertilizing(fertilizingId: Long) {
        cultivationDao?.deleteFertilizingById(fertilizingId)
    }

    suspend fun upsertHarvesting(harvesting: HarvestingEntity): Long {
        val cDao = cultivationDao ?: return 0L
        return if (harvesting.id == 0L) cDao.insertHarvesting(harvesting) else {
            cDao.updateHarvesting(harvesting)
            harvesting.id
        }
    }

    suspend fun deleteHarvesting(harvestingId: Long) {
        cultivationDao?.deleteHarvestingById(harvestingId)
    }

    private suspend fun syncLandFromLahan(lahan: LahanEntity) {
        cultivationDao?.insertLand(
            LandEntity(
                id = lahan.id,
                name = lahan.nama,
                areaSize = lahan.luas,
                areaUnit = lahan.satuan,
                status = lahan.status,
                startDate = lahan.tanggalMulai,
                locationNotes = lahan.lokasi,
                createdAt = lahan.createdAt,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private suspend fun syncCultivationRecordFromProgres(progres: ProgresEntity) {
        val cDao = cultivationDao ?: return
        // Ensure stale synced records from a changed stage are cleaned up
        when (progres.stageType()) {
            StageType.BIBIT, StageType.TANAM -> {
                cDao.deleteFertilizingBySourceProgresId(progres.id)
                cDao.deleteHarvestingBySourceProgresId(progres.id)
                val existing = cDao.getPlantingBySourceProgresId(progres.id)
                val entity = PlantingEntity(
                    id = existing?.id ?: 0L,
                    landId = progres.lahanId,
                    sourceProgresId = progres.id,
                    plantingDate = progres.tanggal,
                    seedQuantity = progres.jumlahBibit ?: 0.0,
                    seedUnit = progres.satuanBibit ?: "Stek",
                    seedSource = progres.sumberAsal ?: "Beli",
                    seedSourceDetail = progres.detailSumber.orEmpty(),
                    plantingSpacing = progres.jarakTanam.orEmpty(),
                    plantingMethod = progres.metodeTanam.orEmpty(),
                    workerCount = progres.tenagaKerja,
                    notes = progres.catatan,
                    createdAt = progres.createdAt
                )
                cDao.insertPlanting(entity)
            }
            StageType.PUPUK_AWAL, StageType.PUPUK_SUSULAN -> {
                cDao.deletePlantingBySourceProgresId(progres.id)
                cDao.deleteHarvestingBySourceProgresId(progres.id)
                val existing = cDao.getFertilizingBySourceProgresId(progres.id)
                val phase = if (progres.stageType() == StageType.PUPUK_AWAL) {
                    FertilizingPhase.INITIAL.name
                } else {
                    FertilizingPhase.FOLLOW_UP.name
                }
                val entity = FertilizingEntity(
                    id = existing?.id ?: 0L,
                    landId = progres.lahanId,
                    sourceProgresId = progres.id,
                    phase = phase,
                    followUpSequence = progres.susulanKe,
                    fertilizingDate = progres.tanggal,
                    fertilizerType = progres.jenisPupuk.orEmpty(),
                    dosageKg = progres.dosisKg ?: 0.0,
                    applicationMethod = progres.caraAplikasi.orEmpty(),
                    notes = progres.catatan,
                    createdAt = progres.createdAt
                )
                cDao.insertFertilizing(entity)
            }
            StageType.PANEN -> {
                cDao.deletePlantingBySourceProgresId(progres.id)
                cDao.deleteFertilizingBySourceProgresId(progres.id)
                val existing = cDao.getHarvestingBySourceProgresId(progres.id)
                val entity = HarvestingEntity(
                    id = existing?.id ?: 0L,
                    landId = progres.lahanId,
                    sourceProgresId = progres.id,
                    harvestNumber = progres.panenKe ?: 1,
                    harvestDate = progres.tanggal,
                    yieldKg = progres.hasilKg ?: 0.0,
                    harvestAgeDays = progres.umurPanenHari,
                    estimatedValueOrUsage = progres.estimasiNilai.orEmpty(),
                    notes = progres.catatan,
                    createdAt = progres.createdAt
                )
                cDao.insertHarvesting(entity)
            }
            StageType.PEMELIHARAAN -> {
                cDao.deletePlantingBySourceProgresId(progres.id)
                cDao.deleteFertilizingBySourceProgresId(progres.id)
                cDao.deleteHarvestingBySourceProgresId(progres.id)
            }
        }
    }

    private suspend fun insertSampleProgres(
        progres: ProgresEntity,
        photos: List<Pair<String, String>>
    ) {
        val id = dao.upsertProgresWithPhotos(progres, photos)
        syncCultivationRecordFromProgres(progres.copy(id = id))
    }

    private suspend fun insertSampleData() {
        val blokALahan = LahanEntity(
            nama = "Blok A - Kebun Timur",
            luas = 1200.0,
            satuan = "m²",
            status = "Siap Panen",
            tanggalMulai = "2026-07-01",
            lokasi = "Desa Sukamaju, dekat saluran irigasi, tanah lempung gembur."
        )
        val blokAId = dao.insertLahan(blokALahan)
        syncLandFromLahan(blokALahan.copy(id = blokAId))

        val blokBLahan = LahanEntity(
            nama = "Blok B - Lahan Kandang",
            luas = 800.0,
            satuan = "m²",
            status = "Aktif Tanam",
            tanggalMulai = "2026-09-20",
            lokasi = "Belakang kandang sapi, akses pupuk kandang sangat dekat."
        )
        val blokBId = dao.insertLahan(blokBLahan)
        syncLandFromLahan(blokBLahan.copy(id = blokBId))

        // --- BLOK A (Siklus Lengkap + 2x Panen + Multi-titik Pertumbuhan) ---
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.BIBIT.name,
                tanggal = "2026-07-01",
                jumlahBibit = 2400.0,
                satuanBibit = "Stek",
                sumberAsal = "Beli",
                detailSumber = "Penangkar Bibit Ciamis (Rp 350/stek, 2-3 mata tunas)",
                catatan = "Bibit segar disortir dan diperam 2 hari di tempat teduh sebelum tanam."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.TANAM.name,
                tanggal = "2026-07-04",
                jarakTanam = "60 x 40 cm",
                metodeTanam = "Miring 45 Derajat",
                tenagaKerja = 3,
                catatan = "Cuaca mendung gerimis, kelembaban tanah ideal untuk perakaran awal."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PUPUK_AWAL.name,
                tanggal = "2026-07-05",
                jenisPupuk = "Pupuk Kandang Sapi Fermentasi + Dolomit",
                dosisKg = 450.0,
                caraAplikasi = "Tebar di Larikan Tanam",
                catatan = "Ditebar merata di larikan tanam untuk menjaga pH dan kegemburan tanah."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PEMELIHARAAN.name,
                tanggal = "2026-07-18",
                umurHst = 14,
                tinggiCm = 35.0,
                kegiatan = "Penyulaman Bibit Mati",
                kondisi = "96% tunas tumbuh serempak",
                catatan = "Tunas muda mulai muncul 3-4 batang per stek."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PEMELIHARAAN.name,
                tanggal = "2026-08-04",
                umurHst = 31,
                tinggiCm = 85.0,
                kegiatan = "Penyiangan Gulma",
                kondisi = "Subur (Hijau Pekat, 8-12 Anakan/Rumpun)",
                catatan = "Penyiangan gulma dan pembumbunan pangkal rumpun."
            ),
            listOf("res://img_odot_banner" to "Kondisi pertumbuhan rapat di Blok A usia 31 HST")
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PUPUK_SUSULAN.name,
                tanggal = "2026-08-08",
                susulanKe = 1,
                jenisPupuk = "Urea + NPK Phonska (2:1)",
                dosisKg = 35.0,
                catatan = "Pemupukan susulan memacu pertumbuhan daun dan batang manis."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PEMELIHARAAN.name,
                tanggal = "2026-08-22",
                umurHst = 49,
                tinggiCm = 132.0,
                kegiatan = "Kontrol Rutin",
                kondisi = "Sangat Subur, daun lebar tanpa hama",
                catatan = "Batang mulai menebal dan siap memasuki fase menjelang panen."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PANEN.name,
                tanggal = "2026-09-08",
                panenKe = 1,
                hasilKg = 3450.0,
                umurPanenHari = 66,
                estimasiNilai = "Rp 2.415.000 (Pakan 14 Ekor Sapi)",
                catatan = "Panen perdana dipotong 8 cm di atas tanah agar tunas baru cepat muncul."
            ),
            listOf("res://img_odot_banner" to "Hamparan Blok A sebelum pemangkasan panen ke-1")
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokAId,
                kategori = StageType.PANEN.name,
                tanggal = "2026-11-05",
                panenKe = 2,
                hasilKg = 4120.0,
                umurPanenHari = 58,
                estimasiNilai = "Rp 2.884.000 (Pakan Ternak & Stek)",
                catatan = "Panen ke-2 lebih lebat karena anakan rumpun bertambah hingga 18 batang."
            ),
            emptyList()
        )

        // --- BLOK B (Lahan Kedua untuk perbandingan grafik) ---
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.BIBIT.name,
                tanggal = "2026-09-20",
                jumlahBibit = 1500.0,
                satuanBibit = "Stek",
                sumberAsal = "Stek Sendiri",
                detailSumber = "Hasil pemangkasan indukan Blok A panen ke-1",
                catatan = "Hemat biaya bibit karena memakai batang bawah dari Blok A."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.TANAM.name,
                tanggal = "2026-09-22",
                jarakTanam = "60 x 40 cm",
                metodeTanam = "Miring 45 Derajat",
                tenagaKerja = 2,
                catatan = "Penanaman serentak di sore hari."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.PUPUK_AWAL.name,
                tanggal = "2026-09-23",
                jenisPupuk = "Kohe Sapi Matang",
                dosisKg = 320.0,
                caraAplikasi = "Sebar di Bedengan",
                catatan = "Pupuk kandang langsung dari area kandang di sebelah lahan."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.PEMELIHARAAN.name,
                tanggal = "2026-10-10",
                umurHst = 18,
                tinggiCm = 42.0,
                kegiatan = "Penyiraman / Irigasi",
                kondisi = "Pertumbuhan tunas merata",
                catatan = "Disiram rutin 2 hari sekali saat cuaca cerah."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.PEMELIHARAAN.name,
                tanggal = "2026-10-28",
                umurHst = 36,
                tinggiCm = 94.0,
                kegiatan = "Pembumbunan Tanah",
                kondisi = "Subur dan rimbun",
                catatan = "Tinggi rata-rata 94 cm pada 36 HST, pertumbuhan sangat responsif."
            ),
            emptyList()
        )
        insertSampleProgres(
            ProgresEntity(
                lahanId = blokBId,
                kategori = StageType.PANEN.name,
                tanggal = "2026-11-14",
                panenKe = 1,
                hasilKg = 2380.0,
                umurPanenHari = 53,
                estimasiNilai = "Rp 1.666.000 (Silase Pakan)",
                catatan = "Panen perdana Blok B umur 53 hari dengan bobot segar 2,38 ton."
            ),
            emptyList()
        )
    }
}
