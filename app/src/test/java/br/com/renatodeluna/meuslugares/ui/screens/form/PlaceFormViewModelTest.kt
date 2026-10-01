package br.com.renatodeluna.meuslugares.ui.screens.form

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
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
class PlaceFormViewModelTest {

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
    fun `formulario novo comeca em branco, sem erro e sem permitir salvar`() {
        val state = PlaceFormViewModel(placeId = null, repository = repository).uiState.value

        assertEquals("", state.name)
        assertFalse(state.isEditing)
        assertNull(state.nameError)
        assertFalse(state.canSave)
    }

    @Test
    fun `nome em branco depois de editado mostra erro e bloqueia o salvar`() {
        val viewModel = PlaceFormViewModel(placeId = null, repository = repository)

        viewModel.onNameChange("Café")
        viewModel.onNameChange("   ")

        assertEquals(R.string.error_name_required, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `salvar com nome invalido nao grava nada`() = testScope.runTest {
        val viewModel = PlaceFormViewModel(placeId = null, repository = repository)

        viewModel.save()

        assertTrue(repository.observePlaces().first().isEmpty())
    }

    @Test
    fun `nota fora da faixa e limitada entre 1 e 5`() {
        val viewModel = PlaceFormViewModel(placeId = null, repository = repository)

        viewModel.onRatingChange(9)
        assertEquals(Place.MAX_RATING, viewModel.uiState.value.rating)

        viewModel.onRatingChange(0)
        assertEquals(Place.MIN_RATING, viewModel.uiState.value.rating)
    }

    @Test
    fun `salvar cria lugar novo com textos aparados e emite evento`() = testScope.runTest {
        val viewModel = PlaceFormViewModel(placeId = null, repository = repository)
        viewModel.onNameChange("  Café Aroma  ")
        viewModel.onCategoryChange(PlaceCategory.CAFE)
        viewModel.onRatingChange(4)
        viewModel.onNotesChange(" Bom espresso ")

        viewModel.save()
        viewModel.savedEvents.first()

        val saved = repository.observePlaces().first().single()
        assertEquals("Café Aroma", saved.name)
        assertEquals(PlaceCategory.CAFE, saved.category)
        assertEquals(4, saved.rating)
        assertEquals("Bom espresso", saved.notes)
        assertTrue(saved.id.isNotBlank())
    }

    @Test
    fun `edicao carrega os campos e salvar preserva id, data, foto e coordenadas`() = testScope.runTest {
        val original = Place(
            id = "abc",
            name = "Loja X",
            category = PlaceCategory.STORE,
            rating = 2,
            notes = "antiga",
            photoUri = "content://foto",
            latitude = -23.5,
            longitude = -46.6,
            createdAt = 123,
        )
        repository.upsert(original)

        val viewModel = PlaceFormViewModel(placeId = "abc", repository = repository)
        val loaded = viewModel.uiState.value
        assertTrue(loaded.isEditing)
        assertEquals("Loja X", loaded.name)
        assertEquals(PlaceCategory.STORE, loaded.category)
        assertEquals(2, loaded.rating)

        viewModel.onNameChange("Loja Y")
        viewModel.onRatingChange(5)
        viewModel.save()
        viewModel.savedEvents.first()

        assertEquals(
            original.copy(name = "Loja Y", rating = 5),
            repository.observePlaces().first().single(),
        )
    }

    @Test
    fun `editar id inexistente sinaliza nao encontrado e nao permite salvar`() {
        val state = PlaceFormViewModel(placeId = "nao-existe", repository = repository).uiState.value

        assertTrue(state.notFound)
        assertFalse(state.canSave)
    }
}
