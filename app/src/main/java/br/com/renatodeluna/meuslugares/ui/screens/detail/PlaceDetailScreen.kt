package br.com.renatodeluna.meuslugares.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import br.com.renatodeluna.meuslugares.ui.components.BackNavigationIcon
import br.com.renatodeluna.meuslugares.ui.components.RatingStars
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun PlaceDetailScreen(
    snackbarHostState: SnackbarHostState,
    onEdit: (placeId: String) -> Unit,
    onDeleted: () -> Unit,
    onBack: () -> Unit,
    viewModel: PlaceDetailViewModel = viewModel(factory = PlaceDetailViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnDeleted by rememberUpdatedState(onDeleted)
    LaunchedEffect(viewModel) {
        viewModel.deletedEvents.collect { currentOnDeleted() }
    }

    PlaceDetailContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEdit = onEdit,
        onConfirmDelete = viewModel::delete,
        onBack = onBack,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailContent(
    uiState: PlaceDetailUiState,
    onEdit: (placeId: String) -> Unit,
    onConfirmDelete: () -> Unit,
    onBack: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_place_detail)) },
                navigationIcon = { BackNavigationIcon(onClick = onBack) },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        val place = uiState.place
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding))
            place == null -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.place_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            else -> PlaceDetails(
                place = place,
                onEdit = { onEdit(place.id) },
                onConfirmDelete = onConfirmDelete,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}

@Composable
private fun PlaceDetails(
    place: Place,
    onEdit: () -> Unit,
    onConfirmDelete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Estado puramente visual; rememberSaveable mantém o diálogo aberto ao girar a tela.
    var showDeleteDialog by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(text = place.name, style = MaterialTheme.typography.headlineSmall)
            Text(
                text = stringResource(place.category.labelRes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }

        RatingStars(rating = place.rating, starSize = 28.dp)

        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = stringResource(R.string.label_notes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = place.notes.ifBlank { stringResource(R.string.detail_no_notes) },
                style = MaterialTheme.typography.bodyLarge,
            )
        }

        Text(
            text = stringResource(R.string.detail_created_at, formatCreatedAt(place.createdAt)),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f),
            ) {
                Text(stringResource(R.string.action_delete))
            }
            Button(onClick = onEdit, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.action_edit))
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_dialog_title)) },
            text = { Text(stringResource(R.string.delete_dialog_message, place.name)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        onConfirmDelete()
                    },
                ) {
                    Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.action_cancel))
                }
            },
        )
    }
}

// O app só tem textos em pt-BR; a data segue o mesmo idioma em vez do locale do aparelho.
private val createdAtFormatter: DateTimeFormatter =
    DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
        .withLocale(Locale.forLanguageTag("pt-BR"))

private fun formatCreatedAt(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).format(createdAtFormatter)

@Preview(showBackground = true)
@Composable
private fun PlaceDetailPreview() {
    MeusLugaresTheme {
        PlaceDetailContent(
            uiState = PlaceDetailUiState(
                place = Place(
                    id = "1",
                    name = "Café Aroma",
                    category = PlaceCategory.CAFE,
                    rating = 4,
                    notes = "Ótimo espresso e pão de queijo.",
                    createdAt = 1_790_000_000_000,
                ),
                isLoading = false,
            ),
            onEdit = {},
            onConfirmDelete = {},
            onBack = {},
        )
    }
}
