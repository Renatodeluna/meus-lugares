package br.com.renatodeluna.meuslugares.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.renatodeluna.meuslugares.R
import br.com.renatodeluna.meuslugares.ui.screens.list.PlacesListScreen

@Composable
fun MeusLugaresNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Screen.PlaceList) {
        composable<Screen.PlaceList> {
            PlacesListScreen(
                onAddPlace = {
                    navController.navigate(Screen.PlaceForm()) { launchSingleTop = true }
                },
                onPlaceClick = { placeId ->
                    navController.navigate(Screen.PlaceDetail(placeId)) { launchSingleTop = true }
                },
            )
        }
        composable<Screen.PlaceForm> { backStackEntry ->
            val route = backStackEntry.toRoute<Screen.PlaceForm>()
            PendingScreen(
                titleRes = if (route.placeId == null) R.string.title_new_place else R.string.title_edit_place,
                onBack = { navController.popBackStack() },
            )
        }
        composable<Screen.PlaceDetail> {
            PendingScreen(
                titleRes = R.string.title_place_detail,
                onBack = { navController.popBackStack() },
            )
        }
    }
}

// Placeholder para as rotas de formulário e detalhes, que ganham telas reais
// nos próximos prompts. Existe só para que a navegação já funcione de ponta a ponta.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PendingScreen(@StringRes titleRes: Int, onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(titleRes)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back),
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(stringResource(R.string.screen_under_construction))
        }
    }
}
