package br.com.renatodeluna.meuslugares.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Place

@Composable
fun RatingStars(
    rating: Int,
    modifier: Modifier = Modifier,
    starSize: Dp = 18.dp,
) {
    val description = stringResource(R.string.rating_description, rating, Place.MAX_RATING)
    // O leitor de tela anuncia "Nota X de 5" uma vez, em vez de cinco ícones soltos.
    Row(modifier = modifier.semantics { contentDescription = description }) {
        for (star in 1..Place.MAX_RATING) {
            Icon(
                painter = painterResource(if (star <= rating) R.drawable.ic_star else R.drawable.ic_star_outline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(starSize),
            )
        }
    }
}
