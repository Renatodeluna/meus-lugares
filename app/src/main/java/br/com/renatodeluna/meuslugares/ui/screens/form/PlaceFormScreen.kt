package br.com.renatodeluna.meuslugares.ui.screens.form

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.domain.model.Coordinates
import br.com.renatodeluna.meuslugares.domain.model.Place
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory
import br.com.renatodeluna.meuslugares.ui.components.BackNavigationIcon
import br.com.renatodeluna.meuslugares.ui.components.LoadingIndicator
import br.com.renatodeluna.meuslugares.ui.components.PlacePhoto
import br.com.renatodeluna.meuslugares.ui.components.formatCoordinates
import br.com.renatodeluna.meuslugares.ui.components.rememberPermissionRequest
import br.com.renatodeluna.meuslugares.ui.theme.MeusLugaresTheme
import kotlinx.coroutines.launch

@Composable
fun PlaceFormScreen(
    snackbarHostState: SnackbarHostState,
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    viewModel: PlaceFormViewModel = viewModel(factory = PlaceFormViewModel.Factory),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val resources = LocalResources.current
    val scope = rememberCoroutineScope()

    fun showMessage(@StringRes messageRes: Int, offerSettings: Boolean = false) {
        scope.launch {
            val result = snackbarHostState.showSnackbar(
                message = resources.getString(messageRes),
                actionLabel = if (offerSettings) resources.getString(R.string.action_settings) else null,
                duration = if (offerSettings) SnackbarDuration.Long else SnackbarDuration.Short,
            )
            if (result == SnackbarResult.ActionPerformed) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:${context.packageName}".toUri())
                )
            }
        }
    }

    val currentOnSaved by rememberUpdatedState(onSaved)
    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                PlaceFormEvent.Saved -> currentOnSaved()
                is PlaceFormEvent.ShowMessage -> showMessage(event.messageRes)
            }
        }
    }

    // Sobrevive à recriação da Activity enquanto o app de câmera está aberto.
    var pendingCaptureUri by rememberSaveable { mutableStateOf<String?>(null) }
    val takePicture = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val uri = pendingCaptureUri
        pendingCaptureUri = null
        if (success && uri != null) viewModel.onPhotoCaptured(uri)
    }

    val requestCamera = rememberPermissionRequest(
        permissions = remember { listOf(Manifest.permission.CAMERA) },
        rationaleTitle = R.string.permission_camera_title,
        rationaleMessage = R.string.permission_camera_rationale,
        onGranted = {
            val uri = viewModel.newCaptureUri()
            pendingCaptureUri = uri
            try {
                takePicture.launch(uri.toUri())
            } catch (e: ActivityNotFoundException) {
                pendingCaptureUri = null
                showMessage(R.string.error_camera_unavailable)
            }
        },
        onDenied = { permanently -> showMessage(R.string.permission_camera_denied, offerSettings = permanently) },
    )

    val requestLocation = rememberPermissionRequest(
        // A COARSE vai junto: a partir do Android 12 o usuário pode escolher só a
        // localização aproximada, e pedir FINE sozinha é ignorado pelo sistema.
        permissions = remember {
            listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        },
        rationaleTitle = R.string.permission_location_title,
        rationaleMessage = R.string.permission_location_rationale,
        onGranted = viewModel::fetchLocation,
        onDenied = { permanently -> showMessage(R.string.permission_location_denied, offerSettings = permanently) },
    )

    PlaceFormContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNameChange = viewModel::onNameChange,
        onCategoryChange = viewModel::onCategoryChange,
        onRatingChange = viewModel::onRatingChange,
        onNotesChange = viewModel::onNotesChange,
        onTakePhoto = requestCamera,
        onRemovePhoto = viewModel::removePhoto,
        onUseLocation = requestLocation,
        onRemoveLocation = viewModel::removeLocation,
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
    onTakePhoto: () -> Unit,
    onRemovePhoto: () -> Unit,
    onUseLocation: () -> Unit,
    onRemoveLocation: () -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
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
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        when {
            uiState.isLoading -> LoadingIndicator(Modifier.padding(innerPadding))
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
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

                FormSection(title = stringResource(R.string.label_photo)) {
                    if (uiState.photoUri != null) {
                        PlacePhoto(
                            uri = uiState.photoUri,
                            contentDescription = stringResource(R.string.photo_description_form),
                            height = 180.dp,
                        )
                    }
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(onClick = onTakePhoto) {
                            Text(
                                stringResource(
                                    if (uiState.photoUri == null) R.string.action_take_photo else R.string.action_retake_photo
                                )
                            )
                        }
                        if (uiState.photoUri != null) {
                            TextButton(onClick = onRemovePhoto) { Text(stringResource(R.string.action_remove_photo)) }
                        }
                    }
                }

                FormSection(title = stringResource(R.string.label_location)) {
                    Text(
                        text = uiState.coordinates?.let { formatCoordinates(it) }
                            ?: stringResource(R.string.location_empty),
                        style = MaterialTheme.typography.bodyLarge,
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        FilledTonalButton(onClick = onUseLocation, enabled = !uiState.isFetchingLocation) {
                            if (uiState.isFetchingLocation) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.padding(end = 8.dp).size(16.dp),
                                )
                            }
                            Text(stringResource(R.string.action_use_location))
                        }
                        if (uiState.coordinates != null) {
                            TextButton(onClick = onRemoveLocation) {
                                Text(stringResource(R.string.action_remove_location))
                            }
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.padding(bottom = 8.dp),
                ) {
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
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
            onTakePhoto = {}, onRemovePhoto = {}, onUseLocation = {}, onRemoveLocation = {},
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
                coordinates = Coordinates(-23.55052, -46.63331),
                isEditing = true,
                isFetchingLocation = true,
            ),
            onNameChange = {}, onCategoryChange = {}, onRatingChange = {}, onNotesChange = {},
            onTakePhoto = {}, onRemovePhoto = {}, onUseLocation = {}, onRemoveLocation = {},
            onSave = {}, onCancel = {},
        )
    }
}
