package br.com.renatodeluna.meuslugares.ui.screens.form

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.data.local.PhotoStorage
import br.com.renatodeluna.meuslugares.data.local.PlacesDataStore
import br.com.renatodeluna.meuslugares.data.repository.PlacesRepository
import br.com.renatodeluna.meuslugares.domain.model.Coordinates
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
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class PlaceFormViewModelTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private val dispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(dispatcher + Job())
    private lateinit var repository: PlacesRepository
    private lateinit var capturesDir: File
    private lateinit var photosDir: File
    private lateinit var photoStorage: PhotoStorage
    private var nextLocation: Coordinates? = null

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        val preferences = PreferenceDataStoreFactory.create(scope = testScope) {
            tmpFolder.root.resolve("test.preferences_pb")
        }
        repository = PlacesRepository(PlacesDataStore(preferences))
        capturesDir = File(tmpFolder.root, "captures")
        photosDir = File(tmpFolder.root, "photos")
        photoStorage = PhotoStorage(capturesDir, photosDir) { "content://test/${it.parentFile!!.name}/${it.name}" }
    }

    @After
    fun tearDown() {
        testScope.cancel()
        Dispatchers.resetMain()
    }

    private fun viewModel(placeId: String? = null) =
        PlaceFormViewModel(placeId, repository, photoStorage) { nextLocation }

    @Test
    fun `formulario novo comeca em branco, sem erro e sem permitir salvar`() {
        val state = viewModel().uiState.value

        assertEquals("", state.name)
        assertFalse(state.isEditing)
        assertNull(state.nameError)
        assertNull(state.photoUri)
        assertNull(state.coordinates)
        assertFalse(state.canSave)
    }

    @Test
    fun `nome em branco depois de editado mostra erro e bloqueia o salvar`() {
        val viewModel = viewModel()

        viewModel.onNameChange("Café")
        viewModel.onNameChange("   ")

        assertEquals(R.string.error_name_required, viewModel.uiState.value.nameError)
        assertFalse(viewModel.uiState.value.canSave)
    }

    @Test
    fun `salvar com nome invalido nao grava nada`() = testScope.runTest {
        viewModel().save()

        assertTrue(repository.observePlaces().first().isEmpty())
    }

    @Test
    fun `nota fora da faixa e limitada entre 1 e 5`() {
        val viewModel = viewModel()

        viewModel.onRatingChange(9)
        assertEquals(Place.MAX_RATING, viewModel.uiState.value.rating)

        viewModel.onRatingChange(0)
        assertEquals(Place.MIN_RATING, viewModel.uiState.value.rating)
    }

    @Test
    fun `salvar cria lugar novo com textos aparados e emite evento`() = testScope.runTest {
        val viewModel = viewModel()
        viewModel.onNameChange("  Café Aroma  ")
        viewModel.onCategoryChange(PlaceCategory.CAFE)
        viewModel.onRatingChange(4)
        viewModel.onNotesChange(" Bom espresso ")

        viewModel.save()

        assertEquals(PlaceFormEvent.Saved, viewModel.events.first())
        val saved = repository.observePlaces().first().single()
        assertEquals("Café Aroma", saved.name)
        assertEquals(PlaceCategory.CAFE, saved.category)
        assertEquals(4, saved.rating)
        assertEquals("Bom espresso", saved.notes)
        assertTrue(saved.id.isNotBlank())
    }

    @Test
    fun `edicao carrega os campos e salvar preserva id e data de cadastro`() = testScope.runTest {
        val original = Place(
            id = "abc",
            name = "Loja X",
            category = PlaceCategory.STORE,
            rating = 2,
            notes = "antiga",
            latitude = -23.5,
            longitude = -46.6,
            createdAt = 123,
        )
        repository.upsert(original)

        val viewModel = viewModel(placeId = "abc")
        val loaded = viewModel.uiState.value
        assertTrue(loaded.isEditing)
        assertEquals("Loja X", loaded.name)
        assertEquals(PlaceCategory.STORE, loaded.category)
        assertEquals(2, loaded.rating)
        assertEquals(Coordinates(-23.5, -46.6), loaded.coordinates)

        viewModel.onNameChange("Loja Y")
        viewModel.onRatingChange(5)
        viewModel.save()
        viewModel.events.first()

        assertEquals(
            original.copy(name = "Loja Y", rating = 5),
            repository.observePlaces().first().single(),
        )
    }

    @Test
    fun `editar id inexistente sinaliza nao encontrado e nao permite salvar`() {
        val state = viewModel(placeId = "nao-existe").uiState.value

        assertTrue(state.notFound)
        assertFalse(state.canSave)
    }

    @Test
    fun `foto capturada e movida do cache para a pasta permanente ao salvar`() = testScope.runTest {
        val viewModel = viewModel()
        val captureUri = viewModel.newCaptureUri()
        File(capturesDir, captureUri.substringAfterLast('/')).writeText("jpeg")
        viewModel.onPhotoCaptured(captureUri)
        viewModel.onNameChange("Mirante")

        viewModel.save()
        viewModel.events.first()

        val photoUri = repository.observePlaces().first().single().photoUri!!
        assertTrue(photoUri.startsWith("content://test/photos/"))
        assertTrue(File(photosDir, photoUri.substringAfterLast('/')).exists())
        assertTrue(capturesDir.listFiles().isNullOrEmpty())
    }

    @Test
    fun `trocar a foto na edicao apaga o arquivo da foto anterior`() = testScope.runTest {
        val oldPhoto = File(photosDir.apply { mkdirs() }, "IMG_old.jpg").apply { writeText("velha") }
        repository.upsert(
            Place("1", "Mirante", PlaceCategory.TOURIST_SPOT, 5, "", photoUri = "content://test/photos/IMG_old.jpg", createdAt = 0)
        )
        val viewModel = viewModel(placeId = "1")
        val captureUri = viewModel.newCaptureUri()
        File(capturesDir, captureUri.substringAfterLast('/')).writeText("nova")
        viewModel.onPhotoCaptured(captureUri)

        viewModel.save()
        viewModel.events.first()

        assertFalse(oldPhoto.exists())
        val newPhotoUri = repository.getById("1")!!.photoUri!!
        assertEquals("nova", File(photosDir, newPhotoUri.substringAfterLast('/')).readText())
    }

    @Test
    fun `remover a foto na edicao salva sem foto e apaga o arquivo`() = testScope.runTest {
        val oldPhoto = File(photosDir.apply { mkdirs() }, "IMG_old.jpg").apply { writeText("velha") }
        repository.upsert(
            Place("1", "Mirante", PlaceCategory.TOURIST_SPOT, 5, "", photoUri = "content://test/photos/IMG_old.jpg", createdAt = 0)
        )
        val viewModel = viewModel(placeId = "1")

        viewModel.removePhoto()
        viewModel.save()
        viewModel.events.first()

        assertNull(repository.getById("1")!!.photoUri)
        assertFalse(oldPhoto.exists())
    }

    @Test
    fun `localizacao obtida preenche as coordenadas que vao para o lugar salvo`() = testScope.runTest {
        nextLocation = Coordinates(-23.55052, -46.63331)
        val viewModel = viewModel()
        viewModel.onNameChange("Praça")

        viewModel.fetchLocation()
        assertEquals(Coordinates(-23.55052, -46.63331), viewModel.uiState.value.coordinates)
        assertFalse(viewModel.uiState.value.isFetchingLocation)

        viewModel.save()
        viewModel.events.first()
        val saved = repository.observePlaces().first().single()
        assertEquals(-23.55052, saved.latitude!!, 0.0)
        assertEquals(-46.63331, saved.longitude!!, 0.0)
    }

    @Test
    fun `falha ao obter localizacao mantem o campo e avisa o usuario`() = testScope.runTest {
        nextLocation = null
        val viewModel = viewModel()

        viewModel.fetchLocation()

        assertNull(viewModel.uiState.value.coordinates)
        assertFalse(viewModel.uiState.value.isFetchingLocation)
        assertEquals(
            PlaceFormEvent.ShowMessage(R.string.error_location_unavailable),
            viewModel.events.first(),
        )
    }

    @Test
    fun `remover localizacao limpa as coordenadas`() {
        nextLocation = Coordinates(1.0, 2.0)
        val viewModel = viewModel()
        viewModel.fetchLocation()

        viewModel.removeLocation()

        assertNull(viewModel.uiState.value.coordinates)
    }
}
