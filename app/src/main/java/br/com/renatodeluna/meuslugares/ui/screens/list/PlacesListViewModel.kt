package br.com.renatodeluna.meuslugares.ui.screens.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import br.com.renatodeluna.meuslugares.MeusLugaresApplication
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class PlacesListViewModel(repository: PlacesRepository) : ViewModel() {

    val uiState: StateFlow<PlacesListUiState> = repository.observePlaces()
        .map { places ->
            PlacesListUiState(
                places = places.sortedByDescending { it.createdAt },
                isLoading = false,
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlacesListUiState(),
        )

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MeusLugaresApplication
                PlacesListViewModel(app.container.placesRepository)
            }
        }
    }
}
