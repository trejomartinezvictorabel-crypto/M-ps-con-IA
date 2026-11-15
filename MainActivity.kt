package mx.campnav.offline

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import mx.campnav.offline.audio.TtsGuideManager
import mx.campnav.offline.location.LocationTracker
import mx.campnav.offline.map.OfflineMapView
import mx.campnav.offline.navigation.NavigationViewModel
import mx.campnav.offline.navigation.TransportMode

class MainActivity : ComponentActivity() {
    private lateinit var tracker: LocationTracker
    private lateinit var tts: TtsGuideManager

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true) tracker.start()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tracker = LocationTracker(this)
        tts = TtsGuideManager(this)

        setContent {
            val vm: NavigationViewModel = viewModel()
            val location by tracker.location.collectAsState()
            val routeState by vm.uiState.collectAsState()

            MaterialTheme {
                Scaffold { padding ->
                    Column(Modifier.fillMaxSize().padding(padding).padding(12.dp)) {
                        Text("CampoNav Offline", style = MaterialTheme.typography.headlineSmall)
                        Text(
                            location?.let {
                                "GPS: %.6f, %.6f | %.1f km/h".format(
                                    it.latitude, it.longitude, it.speedMps * 3.6
                                )
                            } ?: "Ubicación: esperando permiso / señal GPS"
                        )
                        Spacer(Modifier.height(8.dp))
                        OfflineMapView(
                            modifier = Modifier.fillMaxWidth().weight(1f),
                            currentLatitude = location?.latitude,
                            currentLongitude = location?.longitude
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Medio de transporte")
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TransportMode.entries.forEach { mode ->
                                FilterChip(
                                    selected = routeState.mode == mode,
                                    onClick = { vm.setTransportMode(mode) },
                                    label = { Text(mode.label) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(onClick = { requestLocation() }) { Text("Activar GPS") }
                            OutlinedButton(onClick = {
                                val text = routeState.nextInstruction ?: "GPS activo. Sin ruta cargada."
                                tts.speak(text)
                            }) { Text("Probar voz") }
                        }
                        Text(routeState.status)
                        Text("Distancia: ${routeState.remainingMeters.toInt()} m | ETA: ${routeState.etaMinutes} min")
                    }
                }
            }
        }
    }

    private fun requestLocation() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        val granted = permissions.any {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (granted) tracker.start() else locationPermissionLauncher.launch(permissions)
    }

    override fun onResume() {
        super.onResume()
        if (::tracker.isInitialized) {
            val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
            val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
            if (fine || coarse) tracker.start()
        }
    }

    override fun onPause() {
        tracker.stop()
        super.onPause()
    }

    override fun onDestroy() {
        tracker.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
