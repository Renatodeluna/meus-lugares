package br.com.renatodeluna.meuslugares.ui.screens.detail

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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceDetailViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher + Job())
    private lateinit var repository: PlacesRepository

    private val place = Place(
        id = "1",
        name = "Mirante",
        category = PlaceCategory.TOURIST_SPOT,
        rating = 5,
        notes = "",
        createdAt = 0,
    )

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
    fun `carrega o lugar pelo id`() = testScope.runTest {
        repository.upsert(place)

        val state = PlaceDetailViewModel("1", repository).uiState.first { !it.isLoading }

        assertEquals(place, state.place)
        assertFalse(state.notFound)
    }

    @Test
    fun `id inexistente sinaliza nao encontrado`() = testScope.runTest {
        val state = PlaceDetailViewModel("nao-existe", repository).uiState.first { !it.isLoading }

        assertNull(state.place)
        assertTrue(state.notFound)
    }

    @Test
    fun `reflete edicao feita enquanto a tela esta aberta`() = testScope.runTest {
        repository.upsert(place)
        val viewModel = PlaceDetailViewModel("1", repository)
        backgroundScope.launch { viewModel.uiState.collect {} }

        repository.upsert(place.copy(name = "Mirante Novo"))

        assertEquals("Mirante Novo", viewModel.uiState.first { it.place?.name == "Mirante Novo" }.place?.name)
    }

    @Test
    fun `excluir remove do repositorio, emite evento e nao mostra nao encontrado`() = testScope.runTest {
        repository.upsert(place)
        val viewModel = PlaceDetailViewModel("1", repository)
        backgroundScope.launch { viewModel.uiState.collect {} }
        viewModel.uiState.first { !it.isLoading }

        viewModel.delete()
        viewModel.deletedEvents.first()

        assertNull(repository.getById("1"))
        assertEquals(place, viewModel.uiState.value.place)
        assertFalse(viewModel.uiState.value.notFound)
    }
}
