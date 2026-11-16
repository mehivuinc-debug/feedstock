package com.example.ui.viewmodel

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.OdotApplication

/**
 * Centralized ViewModelProvider.Factory providing database-backed repositories
 * from [OdotApplication.container] to Jetpack Compose screens.
 */
object OdotViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer {
            OdotViewModel(odotApplication().container.odotRepository)
        }
    }
}

fun CreationExtras.odotApplication(): OdotApplication {
    return checkNotNull(this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as? OdotApplication) {
        "OdotApplication must be registered in AndroidManifest.xml to use OdotViewModelProvider.Factory"
    }
}
