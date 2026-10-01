package br.com.renatodeluna.meuslugares.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Teal40,
    primaryContainer = TealContainer90,
    onPrimaryContainer = OnTealContainer10,
    secondary = Slate40,
    secondaryContainer = SlateContainer90,
    onSecondaryContainer = OnSlateContainer10,
    tertiary = Coral40,
    tertiaryContainer = CoralContainer90,
    onTertiaryContainer = OnCoralContainer10,
    background = SurfaceLight,
    surface = SurfaceLight,
    onBackground = OnSurfaceLight,
    onSurface = OnSurfaceLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = OnTeal20,
    primaryContainer = TealContainer30,
    onPrimaryContainer = TealContainer90,
    secondary = Slate80,
    onSecondary = OnSlate20,
    secondaryContainer = SlateContainer30,
    onSecondaryContainer = SlateContainer90,
    tertiary = Coral80,
    onTertiary = OnCoral20,
    tertiaryContainer = CoralContainer30,
    onTertiaryContainer = CoralContainer90,
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
