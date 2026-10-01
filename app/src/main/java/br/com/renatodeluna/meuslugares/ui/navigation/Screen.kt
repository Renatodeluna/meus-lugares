package br.com.renatodeluna.meuslugares.ui.navigation

import kotlinx.serialization.Serializable

// Rotas type-safe do Navigation Compose: os argumentos são as propriedades.
sealed interface Screen {

    @Serializable
    data object PlaceList : Screen

    /** `placeId == null` cria um lugar novo; com id, edita o existente. */
    @Serializable
    data class PlaceForm(val placeId: String? = null) : Screen

    @Serializable
    data class PlaceDetail(val placeId: String) : Screen
}
