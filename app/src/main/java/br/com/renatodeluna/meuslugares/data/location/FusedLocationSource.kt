package br.com.renatodeluna.meuslugares.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.SystemClock
import androidx.core.content.ContextCompat
import br.com.renatodeluna.meuslugares.domain.model.Coordinates
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import java.util.concurrent.TimeUnit

class FusedLocationSource(private val context: Context) : LocationSource {

    private val client = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission") // verificada em hasLocationPermission()
    override suspend fun currentLocation(): Coordinates? {
        if (!hasLocationPermission()) return null
        return try {
            // A última localização conhecida é instantânea, mas pode ter horas; se
            // estiver velha (ou não existir, comum em aparelho recém-ligado), pede uma nova.
            val location = client.lastLocation.await()?.takeIf { it.isRecent() }
                ?: withTimeoutOrNull(TimeUnit.SECONDS.toMillis(15)) {
                    val cancellation = CancellationTokenSource()
                    try {
                        client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cancellation.token).await()
                    } finally {
                        // No timeout, para de pedir GPS em vez de deixar o pedido rodando.
                        cancellation.cancel()
                    }
                }
            location?.let { Coordinates(it.latitude, it.longitude) }
        } catch (e: ApiException) {
            null // Google Play Services indisponível ou desatualizado
        } catch (e: SecurityException) {
            null // permissão revogada entre a checagem e a chamada
        }
    }

    private fun hasLocationPermission(): Boolean =
        listOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).any {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }

    private fun Location.isRecent(): Boolean =
        SystemClock.elapsedRealtimeNanos() - elapsedRealtimeNanos < TimeUnit.MINUTES.toNanos(2)
}
