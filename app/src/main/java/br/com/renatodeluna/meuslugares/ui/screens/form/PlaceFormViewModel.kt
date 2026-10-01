package br.com.renatodeluna.meuslugares.ui.screens.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import br.com.renatodeluna.meuslugares.MeusLugaresApplication
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import br.com.renatodeluna.meuslugares.ui.navigation.Screen
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class PlaceFormViewModel(
    private val placeId: String?,
    private val repository: PlacesRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlaceFormUiState(isEditing = placeId != null, isLoading = placeId != null)
    )
    val uiState: StateFlow<PlaceFormUiState> = _uiState.asStateFlow()

    private val _savedEvents = Channel<Unit>(Channel.BUFFERED)
    val savedEvents: Flow<Unit> = _savedEvents.receiveAsFlow()

    // Na edição, o lugar original fornece o que o formulário não mostra
    // (id, createdAt, foto, coordenadas) e precisa ser preservado ao salvar.
    private var original: Place? = null

    init {
        if (placeId != null) {
            viewModelScope.launch {
                val place = repository.getById(placeId)
                original = place
                _uiState.update { state ->
                    if (place == null) {
                        state.copy(isLoading = false, notFound = true)
                    } else {
                        state.copy(
                            name = place.name,
                            category = place.category,
                            rating = place.rating.coerceIn(Place.MIN_RATING, Place.MAX_RATING),
                            notes = place.notes,
                            isLoading = false,
                        )
                    }
                }
            }
        }
    }

    fun onNameChange(name: String) {
        _uiState.update {
            it.copy(name = name, nameError = if (name.isBlank()) R.string.error_name_required else null)
        }
    }

    fun onCategoryChange(category: PlaceCategory) {
        _uiState.update { it.copy(category = category) }
    }

    fun onRatingChange(rating: Int) {
        _uiState.update { it.copy(rating = rating.coerceIn(Place.MIN_RATING, Place.MAX_RATING)) }
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }

        val name = state.name.trim()
        val notes = state.notes.trim()
        val place = original?.copy(
            name = name,
            category = state.category,
            rating = state.rating,
            notes = notes,
        ) ?: Place(
            id = UUID.randomUUID().toString(),
            name = name,
            category = state.category,
            rating = state.rating,
            notes = notes,
            createdAt = System.currentTimeMillis(),
        )

        viewModelScope.launch {
            repository.upsert(place)
            _savedEvents.send(Unit)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MeusLugaresApplication
                val route = createSavedStateHandle().toRoute<Screen.PlaceForm>()
                PlaceFormViewModel(route.placeId, app.container.placesRepository)
            }
        }
    }
}
