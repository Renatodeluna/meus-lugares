package br.com.renatodeluna.meuslugares.ui.screens.form

import androidx.annotation.StringRes
import br.com.renatodeluna.meuslugares.domain.model.PlaceCategory

data class PlaceFormUiState(
    val name: String = "",
    val category: PlaceCategory = PlaceCategory.RESTAURANT,
    val rating: Int = 3,
    val notes: String = "",
    // Só preenchido depois que o usuário mexe no campo: um formulário novo não
    // abre já acusando erro.
    @StringRes val nameError: Int? = null,
    val isEditing: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val notFound: Boolean = false,
) {
    val canSave: Boolean
        get() = name.isNotBlank() && !isLoading && !isSaving && !notFound
}
