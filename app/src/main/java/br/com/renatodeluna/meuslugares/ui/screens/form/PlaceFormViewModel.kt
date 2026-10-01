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
import br.com.renatodeluna.meuslugares.data.local.PhotoStorage
import br.com.renatodeluna.meuslugares.data.location.LocationSource
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
    private val photoStorage: PhotoStorage,
    private val locationSource: LocationSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PlaceFormUiState(isEditing = placeId != null, isLoading = placeId != null)
    )
    val uiState: StateFlow<PlaceFormUiState> = _uiState.asStateFlow()

    private val _events = Channel<PlaceFormEvent>(Channel.BUFFERED)
    val events: Flow<PlaceFormEvent> = _events.receiveAsFlow()

    // Na edição, o lugar original fornece o que o formulário não edita (id,
    // createdAt) e a foto anterior, que é apagada se for trocada.
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
                            photoUri = place.photoUri,
                            coordinates = place.coordinates,
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

    /** URI onde a câmera deve gravar a foto; só vira foto do lugar em [onPhotoCaptured]. */
    fun newCaptureUri(): String = photoStorage.newCaptureUri()

    fun onPhotoCaptured(uri: String) {
        _uiState.update { it.copy(photoUri = uri) }
    }

    fun removePhoto() {
        _uiState.update { it.copy(photoUri = null) }
    }

    fun fetchLocation() {
        if (_uiState.value.isFetchingLocation) return
        _uiState.update { it.copy(isFetchingLocation = true) }
        viewModelScope.launch {
            val coordinates = locationSource.currentLocation()
            _uiState.update {
                it.copy(coordinates = coordinates ?: it.coordinates, isFetchingLocation = false)
            }
            if (coordinates == null) {
                _events.send(PlaceFormEvent.ShowMessage(R.string.error_location_unavailable))
            }
        }
    }

    fun removeLocation() {
        _uiState.update { it.copy(coordinates = null) }
    }

    fun save() {
        val state = _uiState.value
        if (!state.canSave) return
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val photoUri = photoStorage.persist(state.photoUri)
            val place = Place(
                id = original?.id ?: UUID.randomUUID().toString(),
                name = state.name.trim(),
                category = state.category,
                rating = state.rating,
                notes = state.notes.trim(),
                photoUri = photoUri,
                latitude = state.coordinates?.latitude,
                longitude = state.coordinates?.longitude,
                createdAt = original?.createdAt ?: System.currentTimeMillis(),
            )
            repository.upsert(place)

            val previousPhoto = original?.photoUri
            if (previousPhoto != null && previousPhoto != photoUri) {
                photoStorage.delete(previousPhoto)
            }
            _events.send(PlaceFormEvent.Saved)
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as MeusLugaresApplication
                val route = createSavedStateHandle().toRoute<Screen.PlaceForm>()
                PlaceFormViewModel(
                    placeId = route.placeId,
                    repository = app.container.placesRepository,
                    photoStorage = app.container.photoStorage,
                    locationSource = app.container.locationSource,
                )
            }
        }
    }
}
