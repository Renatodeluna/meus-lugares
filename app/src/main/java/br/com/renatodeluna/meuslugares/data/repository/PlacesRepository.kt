package br.com.renatodeluna.meuslugares.data.repository

import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.domain.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

class PlacesRepository(private val dataStore: PlacesDataStore) {

    fun observePlaces(): Flow<List<Place>> = dataStore.places

    suspend fun getById(id: String): Place? =
        dataStore.places.first().find { it.id == id }

    // Toda escrita passa por PlacesDataStore.update, nunca por "first() e depois
    // gravar": a leitura e a gravação precisam estar na mesma transação.
    suspend fun upsert(place: Place) = dataStore.update { places ->
        val index = places.indexOfFirst { it.id == place.id }
        if (index == -1) places + place
        else places.toMutableList().apply { this[index] = place }
    }

    suspend fun delete(id: String) = dataStore.update { places ->
        places.filterNot { it.id == id }
    }
}
