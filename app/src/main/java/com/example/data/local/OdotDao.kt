package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.FotoProgresEntity
import com.example.data.model.LahanEntity
import com.example.data.model.ProgresEntity
import com.example.data.model.ProgresWithPhotos
import kotlinx.coroutines.flow.Flow

@Dao
interface OdotDao {

    @Query("SELECT * FROM lahan ORDER BY createdAt ASC")
    fun observeAllLahan(): Flow<List<LahanEntity>>

    @Query("SELECT COUNT(*) FROM lahan")
    suspend fun countLahan(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLahan(lahan: LahanEntity): Long

    @Update
    suspend fun updateLahan(lahan: LahanEntity)

    @Query("DELETE FROM lahan WHERE id = :lahanId")
    suspend fun deleteLahanById(lahanId: Long)

    @Query("UPDATE lahan SET status = :status WHERE id = :lahanId")
    suspend fun updateLahanStatus(lahanId: Long, status: String)

    @Transaction
    @Query("SELECT * FROM progres ORDER BY tanggal DESC, createdAt DESC")
    fun observeAllProgresWithPhotos(): Flow<List<ProgresWithPhotos>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProgres(progres: ProgresEntity): Long

    @Update
    suspend fun updateProgres(progres: ProgresEntity)

    @Query("DELETE FROM progres WHERE id = :progresId")
    suspend fun deleteProgresById(progresId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhotos(photos: List<FotoProgresEntity>)

    @Query("DELETE FROM foto_progres WHERE progresId = :progresId")
    suspend fun deletePhotosForProgres(progresId: Long)

    @Query("SELECT filePath FROM foto_progres WHERE progresId = :progresId")
    suspend fun getPhotoPathsForProgres(progresId: Long): List<String>

    @Query("SELECT filePath FROM foto_progres WHERE progresId IN (SELECT id FROM progres WHERE lahanId = :lahanId)")
    suspend fun getPhotoPathsForLahan(lahanId: Long): List<String>

    @Query("SELECT filePath FROM foto_progres")
    suspend fun getAllPhotoPaths(): List<String>

    @Query("DELETE FROM lahan")
    suspend fun deleteAllLahan()

    @Transaction
    suspend fun upsertProgresWithPhotos(
        progres: ProgresEntity,
        photoInputs: List<Pair<String, String>>
    ): Long {
        val id = if (progres.id == 0L) {
            insertProgres(progres)
        } else {
            updateProgres(progres)
            deletePhotosForProgres(progres.id)
            progres.id
        }
        if (photoInputs.isNotEmpty()) {
            val entities = photoInputs.map { (path, caption) ->
                FotoProgresEntity(
                    progresId = id,
                    filePath = path,
                    caption = caption
                )
            }
            insertPhotos(entities)
        }
        return id
    }
}
