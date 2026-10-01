package br.com.renatodeluna.meuslugares.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import coil3.compose.AsyncImage

// Coil em vez de BitmapFactory: carrega fora da main thread, reduz a imagem para o
// tamanho exibido e respeita a rotação EXIF que os apps de câmera gravam no JPEG.
@Composable
fun PlacePhoto(
    uri: String,
    contentDescription: String?,
    height: Dp,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        model = uri,
        contentDescription = contentDescription,
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    )
}
