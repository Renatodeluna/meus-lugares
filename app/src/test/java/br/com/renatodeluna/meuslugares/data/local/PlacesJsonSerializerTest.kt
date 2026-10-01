package br.com.renatodeluna.meuslugares.data.local

import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PlacesJsonSerializerTest {

    @Test
    fun `encode e decode preservam todos os campos, inclusive opcionais nulos`() {
        val places = listOf(
            Place(
                id = "1",
                name = "Café do Centro",
                category = PlaceCategory.CAFE,
                rating = 4,
                notes = "Pão de queijo \"excelente\"",
                photoUri = "content://media/1",
                latitude = -23.55,
                longitude = -46.63,
                createdAt = 1_700_000_000_000,
            ),
            Place(
                id = "2",
                name = "Loja X",
                category = PlaceCategory.STORE,
                rating = 3,
                notes = "",
                createdAt = 1_700_000_000_001,
            ),
        )

        assertEquals(places, PlacesJsonSerializer.decode(PlacesJsonSerializer.encode(places)))
    }

    @Test
    fun `JSON corrompido vira lista vazia`() {
        assertTrue(PlacesJsonSerializer.decode("[{\"id\": \"1\", \"name\":").isEmpty())
        assertTrue(PlacesJsonSerializer.decode("{}").isEmpty())
    }

    @Test
    fun `categoria desconhecida vira lista vazia em vez de lancar excecao`() {
        val raw = """[{"id":"1","name":"A","category":"PARK","rating":5,"notes":"","createdAt":0}]"""

        assertTrue(PlacesJsonSerializer.decode(raw).isEmpty())
    }

    @Test
    fun `campo desconhecido e ignorado`() {
        val raw = """[{"id":"1","name":"A","category":"CAFE","rating":5,"notes":"","createdAt":0,"futuro":true}]"""

        assertEquals("A", PlacesJsonSerializer.decode(raw).single().name)
    }

    @Test
    fun `valor ausente ou vazio vira lista vazia`() {
        assertTrue(PlacesJsonSerializer.decode(null).isEmpty())
        assertTrue(PlacesJsonSerializer.decode("").isEmpty())
    }
}
