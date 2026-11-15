package mx.campnav.offline.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class FieldLocation(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float,
    val speedMps: Float,
    val bearingDegrees: Float,
    val timestampMillis: Long
)

class LocationTracker(private val context: Context) : LocationListener {
    private val manager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _location = MutableStateFlow<FieldLocation?>(null)
    val location: StateFlow<FieldLocation?> = _location

    @Suppress("MissingPermission")
    fun start() {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return

        try {
            val provider = when {
                manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
                manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
                else -> return
            }
            // Intervalo conservador para equilibrar precisión y batería en trabajo de campo.
            manager.requestLocationUpdates(provider, 2_000L, 3f, this, Looper.getMainLooper())
            manager.getLastKnownLocation(provider)?.let(::onLocationChanged)
        } catch (_: SecurityException) {
            // El permiso pudo revocarse entre la comprobación y la solicitud.
        } catch (_: IllegalArgumentException) {
            // El proveedor puede no existir en determinados dispositivos.
        }
    }

    fun stop() {
        runCatching { manager.removeUpdates(this) }
    }

    override fun onLocationChanged(location: Location) {
        val previous = _location.value
        // Se descartan lecturas extremadamente imprecisas; el umbral puede ajustarse en campo.
        if (location.accuracy > 100f && previous != null) return
        _location.value = FieldLocation(
            location.latitude, location.longitude, location.accuracy,
            if (location.hasSpeed()) location.speed else 0f,
            if (location.hasBearing()) location.bearing else 0f,
            location.time
        )
    }

    @Deprecated("API heredada")
    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
    override fun onProviderEnabled(provider: String) = Unit
    override fun onProviderDisabled(provider: String) = Unit
}
