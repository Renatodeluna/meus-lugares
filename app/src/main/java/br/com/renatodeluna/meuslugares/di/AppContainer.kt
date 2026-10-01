package br.com.renatodeluna.meuslugares.di

import android.content.Context
import androidx.core.content.FileProvider
import br.com.renatodeluna.meuslugares.data.local.PhotoStorage
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.data.local.placesPreferences
import br.com.renatodeluna.meuslugares.data.location.FusedLocationSource
import br.com.renatodeluna.meuslugares.data.location.LocationSource
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import java.io.File

class AppContainer(context: Context) {
    val placesRepository = PlacesRepository(PlacesDataStore(context.placesPreferences))

    // Pastas com os mesmos nomes dos caminhos em res/xml/file_paths.xml.
    val photoStorage = PhotoStorage(
        capturesDir = File(context.cacheDir, "captures"),
        photosDir = File(context.filesDir, "photos"),
        uriForFile = { file ->
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file).toString()
        },
    )

    val locationSource: LocationSource = FusedLocationSource(context)
}
