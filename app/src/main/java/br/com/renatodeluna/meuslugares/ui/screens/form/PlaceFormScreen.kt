package br.com.renatodeluna.meuslugares.ui.screens.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import br.com.renatodeluna.meuslugares.ui.components.BackNavigationIcon
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme

@Composable
fun PlaceFormScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: PlaceFormViewModel = viewModel(factory = PlaceFormViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(viewModel) {
        viewModel.savedEvents.collect { currentOnSaved() }
    }

    PlaceFormContent(
        uiState = uiState,
        onNameChange = viewModel::onNameChange,
        onCategoryChange = viewModel::onCategoryChange,
        onRatingChange = viewModel::onRatingChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::save,
        onCancel = onCancel,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceFormContent(
    uiState: PlaceFormUiState,
    onNameChange: (String) -> Unit,
    onCategoryChange: (PlaceCategory) -> Unit,
    onRatingChange: (Int) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(stringResource(if (uiState.isEditing) R.string.title_edit_place else R.string.title_new_place))
                },
                navigationIcon = { BackNavigationIcon(onClick = onCancel) },
            )
        },
    ) { innerPadding ->
        when {
            uiState.isLoading -> Box(Modifier.fillMaxSize().padding(innerPadding))
            uiState.notFound -> Box(
                modifier = Modifier.fillMaxSize().padding(innerPadding).padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.place_not_found), style = MaterialTheme.typography.bodyLarge)
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .consumeWindowInsets(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                OutlinedTextField(
                    value = uiState.name,
                    onValueChange = onNameChange,
                    label = { Text(stringResource(R.string.label_name)) },
                    isError = uiState.nameError != null,
                    supportingText = uiState.nameError?.let { error -> { Text(stringResource(error)) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                )

                FormSection(title = stringResource(R.string.label_category)) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlaceCategory.entries.forEach { category ->
                            FilterChip(
                                selected = category == uiState.category,
                                onClick = { onCategoryChange(category) },
                                label = { Text(stringResource(category.labelRes)) },
                            )
                        }
                    }
                }

                FormSection(title = stringResource(R.string.label_rating)) {
                    RatingSelector(rating = uiState.rating, onRatingChange = onRatingChange)
                }

                OutlinedTextField(
                    value = uiState.notes,
                    onValueChange = onNotesChange,
                    label = { Text(stringResource(R.string.label_notes)) },
                    minLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth(),
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedButton(onClick = onCancel, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_cancel))
                    }
                    Button(onClick = onSave, enabled = uiState.canSave, modifier = Modifier.weight(1f)) {
                        Text(stringResource(R.string.action_save))
                    }
                }
            }
        }
    }
}

@Composable
private fun FormSection(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        content()
    }
}

@Composable
private fun RatingSelector(rating: Int, onRatingChange: (Int) -> Unit) {
    Row {
        for (star in Place.MIN_RATING..Place.MAX_RATING) {
            IconButton(
                onClick = { onRatingChange(star) },
                modifier = Modifier.semantics { selected = star == rating },
            ) {
                Icon(
                    painter = painterResource(if (star <= rating) R.drawable.ic_star else R.drawable.ic_star_outline),
                    contentDescription = stringResource(R.string.rating_select, star),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceFormNewPreview() {
    MeusLugaresTheme {
        PlaceFormContent(
            uiState = PlaceFormUiState(name = "", nameError = R.string.error_name_required),
            onNameChange = {}, onCategoryChange = {}, onRatingChange = {}, onNotesChange = {},
            onSave = {}, onCancel = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PlaceFormEditPreview() {
    MeusLugaresTheme {
        PlaceFormContent(
            uiState = PlaceFormUiState(
                name = "Café Aroma",
                category = PlaceCategory.CAFE,
                rating = 4,
                notes = "Ótimo espresso.",
                isEditing = true,
            ),
            onNameChange = {}, onCategoryChange = {}, onRatingChange = {}, onNotesChange = {},
            onSave = {}, onCancel = {},
        )
    }
}
