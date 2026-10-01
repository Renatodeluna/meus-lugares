package br.com.renatodeluna.meuslugares.data.local

import br.com.renatodeluna.meuslugares.domain.model.Place
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object PlacesJsonSerializer {

    // ignoreUnknownKeys: um JSON salvo por uma versão futura do app (com campos
    // novos) continua legível por esta versão.
    private val json = Json { ignoreUnknownKeys = true }

    fun encode(places: List<Place>): String = json.encodeToString(places)

    /**
     * JSON ausente ou corrompido vira lista vazia, para que um dado ruim no
     * disco nunca derrube o app na abertura.
     */
    fun decode(raw: String?): List<Place> {
        if (raw.isNullOrBlank()) return emptyList()
        return try {
            json.decodeFromString<List<Place>>(raw)
        } catch (e: SerializationException) {
            emptyList()
        } catch (e: IllegalArgumentException) {
            emptyList()
        }
    }
}
