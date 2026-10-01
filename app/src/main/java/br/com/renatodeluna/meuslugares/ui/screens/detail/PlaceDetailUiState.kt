package br.com.renatodeluna.meuslugares.ui.screens.detail

import br.com.renatodeluna.meuslugares.domain.model.Place

data class PlaceDetailUiState(
    val place: Place? = null,
    val isLoading: Boolean = true,
) {
    val notFound: Boolean
        get() = !isLoading && place == null
}
