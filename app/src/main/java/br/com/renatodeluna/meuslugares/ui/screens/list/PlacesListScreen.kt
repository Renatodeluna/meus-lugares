package br.com.renatodeluna.meuslugares.ui.screens.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import br.com.renatodeluna.meuslugares.ui.components.PlaceCard
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme

@Composable
fun PlacesListScreen(
    onAddPlace: () -> Unit,
    onPlaceClick: (placeId: String) -> Unit,
    viewModel: PlacesListViewModel = viewModel(factory = PlacesListViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    PlacesListContent(
        uiState = uiState,
        onAddPlace = onAddPlace,
        onPlaceClick = onPlaceClick,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlacesListContent(
    uiState: PlacesListUiState,
    onAddPlace: () -> Unit,
    onPlaceClick: (placeId: String) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddPlace) {
                Icon(
                    painter = painterResource(R.drawable.ic_add),
                    contentDescription = stringResource(R.string.action_add_place),
                )
            }
        },
    ) { innerPadding ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
        when {
            // A leitura do DataStore leva milissegundos; um spinner só piscaria na tela.
            uiState.isLoading -> Box(contentModifier)
            uiState.places.isEmpty() -> EmptyPlaces(contentModifier)
            else -> LazyColumn(
                modifier = contentModifier,
                // Espaço extra embaixo para o último card não ficar atrás do FAB.
                contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                items(uiState.places, key = { it.id }) { place ->
                    PlaceCard(place = place, onClick = { onPlaceClick(place.id) })
                }
            }
        }
    }
}

@Composable
private fun EmptyPlaces(modifier: Modifier = Modifier) {
    Box(modifier = modifier.padding(32.dp), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = stringResource(R.string.places_empty_title),
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.places_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlacesListEmptyPreview() {
    MeusLugaresTheme {
        PlacesListContent(
            uiState = PlacesListUiState(isLoading = false),
            onAddPlace = {},
            onPlaceClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlacesListPreview() {
    MeusLugaresTheme {
        PlacesListContent(
            uiState = PlacesListUiState(
                places = listOf(
                    Place("1", "Restaurante Sabor", PlaceCategory.RESTAURANT, 5, "Melhor feijoada da cidade.", createdAt = 2),
                    Place("2", "Café Aroma", PlaceCategory.CAFE, 4, "", createdAt = 1),
                    Place("3", "Mirante da Serra", PlaceCategory.TOURIST_SPOT, 3, "Ir no fim da tarde.", createdAt = 0),
                ),
                isLoading = false,
            ),
            onAddPlace = {},
            onPlaceClick = {},
        )
    }
}
