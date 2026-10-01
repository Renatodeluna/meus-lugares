package br.com.renatodeluna.meuslugares.domain.model

import androidx.annotation.StringRes
import br.com.renatodeluna.meuslugares.R
import kotlinx.serialization.Serializable

// Serializado pelo nome da constante: renomear uma constante quebra os dados já salvos.
@Serializable
enum class PlaceCategory(@StringRes val labelRes: Int) {
    RESTAURANT(R.string.category_restaurant),
    CAFE(R.string.category_cafe),
    TOURIST_SPOT(R.string.category_tourist_spot),
    STORE(R.string.category_store),
}
