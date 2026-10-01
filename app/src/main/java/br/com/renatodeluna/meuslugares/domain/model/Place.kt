package br.com.renatodeluna.meuslugares.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Place(
    val id: String,
    val name: String,
    val category: PlaceCategory,
    val rating: Int,
    val notes: String,
    val photoUri: String? = null,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val createdAt: Long,
) {
    // Propriedade sem campo de apoio: não entra no JSON salvo.
    val coordinates: Coordinates?
        get() = if (latitude != null && longitude != null) Coordinates(latitude, longitude) else null

    // A faixa é validada no PlaceFormViewModel; aqui não há require para que um
    // registro antigo fora da faixa não impeça a leitura da lista inteira.
    companion object {
        const val MIN_RATING = 1
        const val MAX_RATING = 5
    }
}
