package br.com.renatodeluna.meuslugares.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import br.com.renatodeluna.meuslugares.domain.model.Place
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

// O delegate garante uma única instância de DataStore por arquivo no processo;
// duas instâncias apontando para o mesmo arquivo lançam exceção.
val Context.placesPreferences: DataStore<Preferences> by preferencesDataStore(name = "places")

class PlacesDataStore(private val dataStore: DataStore<Preferences>) {

    val places: Flow<List<Place>> = dataStore.data
        .catch { e ->
            if (e is IOException) emit(emptyPreferences()) else throw e
        }
        .map { prefs -> PlacesJsonSerializer.decode(prefs[PLACES_KEY]) }

    /**
     * Lê a lista atual, aplica [transform] e grava o resultado numa única
     * transação do DataStore.
     */
    suspend fun update(transform: (List<Place>) -> List<Place>) {
        dataStore.edit { prefs ->
            val current = PlacesJsonSerializer.decode(prefs[PLACES_KEY])
            prefs[PLACES_KEY] = PlacesJsonSerializer.encode(transform(current))
        }
    }

    internal companion object {
        val PLACES_KEY = stringPreferencesKey("places_json")
    }
}
