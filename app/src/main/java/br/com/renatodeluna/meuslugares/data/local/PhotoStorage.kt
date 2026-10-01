package br.com.renatodeluna.meuslugares.data.local

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

/**
 * Fotos tiradas pela câmera vão primeiro para [capturesDir] (cache), já que o
 * usuário ainda pode cancelar o formulário; ao salvar o lugar, [persist] move a
 * foto para [photosDir] (armazenamento interno), que o sistema não limpa.
 *
 * As URIs são `content://` do FileProvider. [uriForFile] precisa gerar URIs cujo
 * penúltimo segmento é o nome da pasta ("captures"/"photos"), como em
 * `res/xml/file_paths.xml` — é assim que uma URI salva é mapeada de volta para o arquivo.
 */
class PhotoStorage(
    private val capturesDir: File,
    private val photosDir: File,
    private val uriForFile: (File) -> String,
) {

    /** URI de destino para a câmera gravar uma nova foto. */
    fun newCaptureUri(): String {
        capturesDir.mkdirs()
        return uriForFile(File(capturesDir, "IMG_${UUID.randomUUID()}.jpg"))
    }

    /**
     * Garante que a foto fique no armazenamento permanente. Retorna a URI final,
     * ou null se a captura não existe mais (ex.: cache limpo antes de salvar).
     */
    suspend fun persist(uri: String?): String? = withContext(Dispatchers.IO) {
        if (uri == null) return@withContext null
        val capture = fileIn(capturesDir, uri) ?: return@withContext uri
        if (!capture.exists()) return@withContext null

        photosDir.mkdirs()
        val photo = File(photosDir, capture.name)
        capture.copyTo(photo, overwrite = true)
        capture.delete()
        uriForFile(photo)
    }

    /** Apaga uma foto já persistida; capturas temporárias ficam para o sistema limpar. */
    suspend fun delete(uri: String?) {
        withContext(Dispatchers.IO) {
            uri?.let { fileIn(photosDir, it) }?.delete()
        }
    }

    private fun fileIn(dir: File, uri: String): File? {
        val dirName = uri.substringBeforeLast('/').substringAfterLast('/')
        if (dirName != dir.name) return null
        val file = File(dir, uri.substringAfterLast('/'))
        // Defesa contra um nome como ".." vindo de dados corrompidos.
        return file.takeIf { it.canonicalFile.parentFile == dir.canonicalFile }
    }
}
