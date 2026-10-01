package br.com.renatodeluna.meuslugares.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.com.renatodeluna.meuslugares.MeusLugaresApplication
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import br.com.renatodeluna.meuslugares.ui.navigation.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.runningFold
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class PlaceDetailViewModel(
    private val placeId: String,
    private val repository: PlacesRepository,
) : ViewModel() {

    private var isDeleting = false

    // Observa o repositório (e não um getById único) para que a tela reflita na
    // hora uma edição salva no formulário.
    val uiState: StateFlow<PlaceDetailUiState> = repository.observePlaces()
        .map { places -> places.find { it.id == placeId } }
        .runningFold(PlaceDetailUiState()) { previous, place ->
            when {
                place != null -> PlaceDetailUiState(place = place, isLoading = false)
                // Ao excluir, o lugar some do repositório antes de a navegação de
                // volta terminar; manter o último estado evita piscar "não encontrado".
                isDeleting -> previous
                else -> PlaceDetailUiState(place = null, isLoading = false)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PlaceDetailUiState(),
        )

    private val _deletedEvents = Channel<Unit>(Channel.BUFFERED)
    val deletedEvents: Flow<Unit> = _deletedEvents.receiveAsFlow()

    fun delete() {
        if (isDeleting) return
        isDeleting = true
        viewModelScope.launch {
            repository.delete(placeId)
            _deletedEvents.send(Unit)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MeusLugaresApplication
                val route = createSavedStateHandle().toRoute<Screen.PlaceDetail>()
                PlaceDetailViewModel(route.placeId, app.container.placesRepository)
            }
        }
    }
}
