package br.com.renatodeluna.meuslugares.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesRepositoryTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val testScope = TestScope(UnconfinedTestDispatcher() + Job())
    private lateinit var preferences: DataStore<Preferences>
    private lateinit var repository: PlacesRepository

    @Before
    fun setUp() {
        preferences = PreferenceDataStoreFactory.create(scope = testScope) {
            tmpFolder.root.resolve("test.preferences_pb")
        }
        repository = PlacesRepository(PlacesDataStore(preferences))
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `lugares inseridos sao lidos de volta na mesma ordem`() = testScope.runTest {
        val samples = listOf(
            place("1", "Restaurante Sabor", PlaceCategory.RESTAURANT),
            place("2", "Café Aroma", PlaceCategory.CAFE),
            place("3", "Mirante da Serra", PlaceCategory.TOURIST_SPOT),
        )

        samples.forEach { repository.upsert(it) }

        assertEquals(samples, repository.observePlaces().first())
        assertEquals(samples[1], repository.getById("2"))
    }

    @Test
    fun `upsert com id existente substitui sem duplicar nem mudar a posicao`() = testScope.runTest {
        repository.upsert(place("1", "A"))
        repository.upsert(place("2", "B"))

        repository.upsert(place("1", "A editado"))

        assertEquals(listOf("A editado", "B"), repository.observePlaces().first().map { it.name })
    }

    @Test
    fun `delete remove apenas o lugar com o id informado`() = testScope.runTest {
        repository.upsert(place("1", "A"))
        repository.upsert(place("2", "B"))

        repository.delete("1")

        assertNull(repository.getById("1"))
        assertEquals(listOf("2"), repository.observePlaces().first().map { it.id })
    }

    @Test
    fun `getById de id inexistente retorna null`() = testScope.runTest {
        assertNull(repository.getById("nao-existe"))
    }

    @Test
    fun `JSON corrompido no disco e lido como lista vazia`() = testScope.runTest {
        preferences.edit { it[PlacesDataStore.PLACES_KEY] = "{lixo" }

        assertTrue(repository.observePlaces().first().isEmpty())
    }

    @Test
    fun `upserts concorrentes nao perdem nenhuma escrita`() = testScope.runTest {
        withContext(Dispatchers.Default) {
            (1..50).map { i -> async { repository.upsert(place("$i", "Lugar $i")) } }.awaitAll()
        }

        assertEquals(50, repository.observePlaces().first().size)
    }

    private fun place(id: String, name: String, category: PlaceCategory = PlaceCategory.CAFE) = Place(
        id = id,
        name = name,
        category = category,
        rating = 5,
        notes = "",
        createdAt = 0,
    )
}
