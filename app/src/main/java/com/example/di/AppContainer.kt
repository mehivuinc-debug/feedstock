package com.example.di

import android.content.Context
import com.example.data.local.OdotDatabase
import com.example.data.local.PhotoStorageManager
import com.example.data.repository.OdotRepository

/**
 * Dependency injection container managing application-scoped Room database,
 * photo storage, and repository instances.
 */
interface AppContainer {
    val database: OdotDatabase
    val photoStorageManager: PhotoStorageManager
    val odotRepository: OdotRepository
}

class DefaultAppContainer(private val context: Context) : AppContainer {

    override val database: OdotDatabase by lazy {
        OdotDatabase.getInstance(context)
    }

    override val photoStorageManager: PhotoStorageManager by lazy {
        PhotoStorageManager(context)
    }

    override val odotRepository: OdotRepository by lazy {
        OdotRepository(
            dao = database.odotDao(),
            photoStorageManager = photoStorageManager,
            cultivationDao = database.cultivationDao()
        )
    }
}
