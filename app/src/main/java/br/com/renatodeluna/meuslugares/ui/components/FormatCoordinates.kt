package br.com.renatodeluna.meuslugares.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Coordinates
import java.util.Locale

// 5 casas decimais ≈ 1 metro de precisão, mais que suficiente para um lugar.
// pt-BR fixo, como as datas: o app só tem textos em pt-BR.
private val ptBr = Locale.forLanguageTag("pt-BR")

@Composable
fun formatCoordinates(coordinates: Coordinates): String = stringResource(
    R.string.location_value,
    String.format(ptBr, "%.5f", coordinates.latitude),
    String.format(ptBr, "%.5f", coordinates.longitude),
)
