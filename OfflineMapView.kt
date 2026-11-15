package mx.campnav.offline.map

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import java.io.File

@Composable
fun OfflineMapView(
    modifier: Modifier = Modifier,
    currentLatitude: Double? = null,
    currentLongitude: Double? = null
) {
    val context = LocalContext.current
    AndroidView(
        modifier = modifier,
        factory = {
            val base = File(context.getExternalFilesDir(null), "osmdroid")
            base.mkdirs()
            Configuration.getInstance().apply {
                osmdroidBasePath = base
                osmdroidTileCache = File(base, "tiles").apply { mkdirs() }
                userAgentValue = context.packageName
            }
            MapView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                setMultiTouchControls(true)
                controller.setZoom(13.0)
                controller.setCenter(GeoPoint(23.6345, -102.5528)) // Centro aproximado de México
                // Teselas locales XYZ: files/tiles/{z}/{x}/{y}.png
                setTileSource(XYTileSource("LocalXYZ", 0, 19, 256, ".png", arrayOf()))
                setUseDataConnection(false)
                invalidate()
            }
        },
        update = { map ->
            if (currentLatitude != null && currentLongitude != null) {
                map.controller.animateTo(GeoPoint(currentLatitude, currentLongitude))
            }
        },
        onRelease = { it.onDetach() }
    )
}
