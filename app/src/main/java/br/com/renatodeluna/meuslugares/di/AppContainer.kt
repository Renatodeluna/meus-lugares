package br.com.renatodeluna.meuslugares.di

import android.content.Context
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.data.local.placesPreferences
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository

class AppContainer(context: Context) {
    val placesRepository = PlacesRepository(PlacesDataStore(context.placesPreferences))
}
