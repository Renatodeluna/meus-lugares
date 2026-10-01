package br.com.renatodeluna.meuslugares.data.location

import br.com.renatodeluna.meuslugares.domain.model.Coordinates

// fun interface para os testes do ViewModel passarem uma lambda no lugar do GPS.
fun interface LocationSource {
    /** Localização atual, ou null se não houver permissão, GPS desligado ou falha. */
    suspend fun currentLocation(): Coordinates?
}
