package br.com.renatodeluna.meuslugares.ui.components

import android.content.pm.PackageManager
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.StringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import br.com.renatodeluna.meuslugares.R

/**
 * Retorna uma ação que garante a permissão antes de executar [onGranted]:
 * - já concedida → executa direto;
 * - negada antes (o sistema recomenda explicar) → mostra um diálogo com o motivo;
 * - caso contrário → abre o pedido do sistema.
 *
 * Basta uma das [permissions] ser concedida (ex.: localização aproximada no lugar
 * da precisa). [onDenied] recebe `permanently = true` quando o sistema não vai mais
 * mostrar o pedido e só resta o usuário liberar nas configurações.
 */
@Composable
fun rememberPermissionRequest(
    permissions: List<String>,
    @StringRes rationaleTitle: Int,
    @StringRes rationaleMessage: Int,
    onGranted: () -> Unit,
    onDenied: (permanently: Boolean) -> Unit,
): () -> Unit {
    val context = LocalContext.current
    val activity = LocalActivity.current
    val currentOnGranted by rememberUpdatedState(onGranted)
    val currentOnDenied by rememberUpdatedState(onDenied)
    var showRationale by rememberSaveable { mutableStateOf(false) }

    val shouldExplain = {
        activity != null && permissions.any { ActivityCompat.shouldShowRequestPermissionRationale(activity, it) }
    }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) currentOnGranted() else currentOnDenied(!shouldExplain())
    }

    if (showRationale) {
        AlertDialog(
            onDismissRequest = {
                showRationale = false
                currentOnDenied(false)
            },
            title = { Text(stringResource(rationaleTitle)) },
            text = { Text(stringResource(rationaleMessage)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        launcher.launch(permissions.toTypedArray())
                    },
                ) { Text(stringResource(R.string.action_continue)) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showRationale = false
                        currentOnDenied(false)
                    },
                ) { Text(stringResource(R.string.action_not_now)) }
            },
        )
    }

    return remember(permissions) {
        {
            val granted = permissions.any {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
            when {
                granted -> currentOnGranted()
                shouldExplain() -> showRationale = true
                else -> launcher.launch(permissions.toTypedArray())
            }
        }
    }
}
