package br.com.renatodeluna.meuslugares.data.local

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class PhotoStorageTest {

    @get:Rule
    val tmpFolder = TemporaryFolder()

    private lateinit var capturesDir: File
    private lateinit var photosDir: File
    private lateinit var storage: PhotoStorage

    @Before
    fun setUp() {
        capturesDir = File(tmpFolder.root, "cache/captures")
        photosDir = File(tmpFolder.root, "files/photos")
        storage = PhotoStorage(capturesDir, photosDir, ::fakeUri)
    }

    @Test
    fun `persist move a captura do cache para a pasta permanente`() = runTest {
        val captureUri = storage.newCaptureUri()
        val capture = File(capturesDir, captureUri.substringAfterLast('/')).apply { writeText("jpeg") }

        val photoUri = storage.persist(captureUri)

        val photo = File(photosDir, capture.name)
        assertEquals(fakeUri(photo), photoUri)
        assertEquals("jpeg", photo.readText())
        assertFalse(capture.exists())
    }

    @Test
    fun `persist de foto ja permanente retorna a mesma uri`() = runTest {
        val photo = File(photosDir.apply { mkdirs() }, "IMG_1.jpg").apply { writeText("jpeg") }

        assertEquals(fakeUri(photo), storage.persist(fakeUri(photo)))
    }

    @Test
    fun `persist de captura que sumiu do cache retorna null`() = runTest {
        assertNull(storage.persist(storage.newCaptureUri()))
    }

    @Test
    fun `persist de null retorna null`() = runTest {
        assertNull(storage.persist(null))
    }

    @Test
    fun `delete apaga a foto permanente`() = runTest {
        val photo = File(photosDir.apply { mkdirs() }, "IMG_1.jpg").apply { writeText("jpeg") }

        storage.delete(fakeUri(photo))

        assertFalse(photo.exists())
    }

    @Test
    fun `delete ignora uri que tenta sair da pasta de fotos`() = runTest {
        val outside = File(tmpFolder.root, "files/places.preferences_pb").apply {
            parentFile!!.mkdirs()
            writeText("dados")
        }

        storage.delete("content://test/photos/..")
        storage.delete("content://test/files/places.preferences_pb")

        assertTrue(outside.exists())
    }

    private fun fakeUri(file: File) = "content://test/${file.parentFile!!.name}/${file.name}"
}
