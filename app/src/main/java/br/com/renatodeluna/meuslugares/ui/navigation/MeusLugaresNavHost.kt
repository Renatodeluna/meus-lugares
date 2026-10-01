package br.com.renatodeluna.meuslugares.ui.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.ui.screens.detail.PlaceDetailScreen
import br.com.renatodeluna.meuslugares.ui.screens.form.PlaceFormScreen
import br.com.renatodeluna.meuslugares.ui.screens.list.PlacesListScreen
import kotlinx.coroutines.launch

@Composable
fun MeusLugaresNavHost(navController: NavHostController = rememberNavController()) {
    // Compartilhado entre as telas: o feedback de "salvo"/"excluído" precisa
    // aparecer na tela de destino, já que a tela que disparou a ação é fechada.
    // O escopo é o do NavHost para o Snackbar sobreviver à saída dessa tela.
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val savedMessage = stringResource(R.string.place_saved)
    val deletedMessage = stringResource(R.string.place_deleted)
    val showMessage: (String) -> Unit = { message ->
        scope.launch { snackbarHostState.showSnackbar(message) }
    }

    NavHost(navController = navController, startDestination = Screen.PlaceList) {
        composable<Screen.PlaceList> {
            PlacesListScreen(
                snackbarHostState = snackbarHostState,
                onAddPlace = {
                    navController.navigate(Screen.PlaceForm()) { launchSingleTop = true }
                },
                onPlaceClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail(placeId)) { launchSingleTop = true }
                },
            )
        }
        composable<Screen.PlaceForm> {
            PlaceFormScreen(
                onSaved = {
                    navController.popBackStack()
                    showMessage(savedMessage)
                },
                onCancel = { navController.popBackStack() },
            )
        }
        composable<Screen.PlaceDetail> {
            PlaceDetailScreen(
                snackbarHostState = snackbarHostState,
                onEdit = { placeId ->
                    navController.navigate(Screen.PlaceForm(placeId)) { launchSingleTop = true }
                },
                onDeleted = {
                    navController.popBackStack<Screen.PlaceList>(inclusive = false)
                    showMessage(deletedMessage)
                },
                onBack = { navController.popBackStack() },
            )
        }
    }
}
