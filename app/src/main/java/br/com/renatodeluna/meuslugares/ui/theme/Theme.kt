package br.com.renatodeluna.meuslugares.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    primaryContainer = TealContainer90,
    secondary = Slate40,
    secondaryContainer = SlateContainer90,
    tertiary = Coral40,
    tertiaryContainer = CoralContainer90,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    primaryContainer = TealContainer30,
    secondary = Slate80,
    secondaryContainer = SlateContainer30,
    tertiary = Coral80,
    tertiaryContainer = CoralContainer30,
    background = SurfaceDark,
    surface = SurfaceDark,
    onBackground = OnSurfaceDark,
    onSurface = OnSurfaceDark,
)

// Dynamic color desativado de propósito: o app usa sempre a mesma paleta.
@Composable
fun MeusLugaresTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
