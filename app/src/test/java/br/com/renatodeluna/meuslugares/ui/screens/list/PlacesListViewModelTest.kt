package br.com.renatodeluna.meuslugares.ui.screens.list

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PlacesListViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher + Job())
    private lateinit var repository: PlacesRepository

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val preferences = PreferenceDataStoreFactory.create(scope = testScope) {
            tmpFolder.root.resolve("test.preferences_pb")
        }
        repository = PlacesRepository(PlacesDataStore(preferences))
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    @Test
    fun `comeca carregando e depois expoe lista vazia na primeira execucao`() = testScope.runTest {
        val viewModel = PlacesListViewModel(repository)
        assertTrue(viewModel.uiState.value.isLoading)

        val loaded = viewModel.uiState.first { !it.isLoading }

        assertTrue(loaded.places.isEmpty())
    }

    @Test
    fun `lista os lugares do mais recente para o mais antigo`() = testScope.runTest {
        repository.upsert(place("antigo", createdAt = 1))
        repository.upsert(place("novo", createdAt = 3))
        repository.upsert(place("meio", createdAt = 2))

        val viewModel = PlacesListViewModel(repository)
        val loaded = viewModel.uiState.first { !it.isLoading }

        assertEquals(listOf("novo", "meio", "antigo"), loaded.places.map { it.id })
    }

    @Test
    fun `reflete um lugar inserido depois que a tela ja esta observando`() = testScope.runTest {
        val viewModel = PlacesListViewModel(repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.uiState.first { !it.isLoading }

        repository.upsert(place("1", createdAt = 1))

        val updated = viewModel.uiState.first { it.places.isNotEmpty() }
        assertEquals("1", updated.places.single().id)
        assertFalse(updated.isLoading)
    }

    private fun place(id: String, createdAt: Long) = Place(
        id = id,
        name = "Lugar $id",
        category = PlaceCategory.RESTAURANT,
        rating = 3,
        notes = "",
        createdAt = createdAt,
    )
}
