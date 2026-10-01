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
    inversePrimary = Teal80,
    secondary = Slate40,
    secondaryContainer = SlateContainer90,
    onSecondaryContainer = OnSlateContainer10,
    tertiary = Coral40,
    tertiaryContainer = CoralContainer90,
    onTertiaryContainer = OnCoralContainer10,
    background = SurfaceLight,
    onBackground = OnSurfaceLight,
    surface = SurfaceLight,
    onSurface = OnSurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantLight,
    surfaceTint = Teal40,
    inverseSurface = InverseSurfaceLight,
    inverseOnSurface = InverseOnSurfaceLight,
    outline = OutlineLight,
    outlineVariant = OutlineVariantLight,
    surfaceBright = SurfaceLight,
    surfaceDim = SurfaceDimLight,
    surfaceContainerLowest = SurfaceContainerLowestLight,
    surfaceContainerLow = SurfaceContainerLowLight,
    surfaceContainer = SurfaceContainerLight,
    surfaceContainerHigh = SurfaceContainerHighLight,
    surfaceContainerHighest = SurfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = Teal80,
    onPrimary = OnTeal20,
    primaryContainer = TealContainer30,
    onPrimaryContainer = TealContainer90,
    inversePrimary = Teal40,
    secondary = Slate80,
    onSecondary = OnSlate20,
    secondaryContainer = SlateContainer30,
    onSecondaryContainer = SlateContainer90,
    tertiary = Coral80,
    onTertiary = OnCoral20,
    tertiaryContainer = CoralContainer30,
    onTertiaryContainer = CoralContainer90,
    background = SurfaceDark,
    onBackground = OnSurfaceDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    surfaceTint = Teal80,
    inverseSurface = InverseSurfaceDark,
    inverseOnSurface = InverseOnSurfaceDark,
    outline = OutlineDark,
    outlineVariant = OutlineVariantDark,
    surfaceBright = SurfaceBrightDark,
    surfaceDim = SurfaceDark,
    surfaceContainerLowest = SurfaceContainerLowestDark,
    surfaceContainerLow = SurfaceContainerLowDark,
    surfaceContainer = SurfaceContainerDark,
    surfaceContainerHigh = SurfaceContainerHighDark,
    surfaceContainerHighest = SurfaceContainerHighestDark,
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
