package br.com.renatodeluna.meuslugares.ui.screens.list

import br.com.renatodeluna.meuslugares.domain.model.Place

data class PlacesListUiState(
    val places: List<Place> = emptyList(),
    // Distingue "ainda lendo do disco" de "lista realmente vazia", para a mensagem
    // de estado vazio não piscar na abertura quando já existem lugares salvos.
    val isLoading: Boolean = true,
)
