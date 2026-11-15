# CampoNav Offline

Aplicación Android nativa para navegación y captura de datos de campo en México.

## Requisitos de compilación
- JDK 17
- Android SDK con plataformas 34 y 35 instaladas
- Android Gradle Plugin 8.7.3
- Gradle 8.9 o compatible con AGP 8.7.3
- Android Studio reciente o compilador CI que ejecute Gradle

El `targetSdk` está fijado en 34. `compileSdk` usa 35 para compatibilidad con las herramientas modernas; se puede cambiar a 34 si el entorno solo tiene esa plataforma instalada.

## Compilar
Desde la raíz del proyecto:
```bash
gradle :app:assembleDebug
```
APK resultante:
`app/build/outputs/apk/debug/app-debug.apk`

## Mapas completamente offline
El visor está configurado para NO usar conexión de datos (`setUseDataConnection(false)`).
Coloca las teselas raster XYZ previamente descargadas en:
`Android/data/mx.campnav.offline/files/osmdroid/tiles/{z}/{x}/{y}.png`

La carpeta puede prepararse mediante un instalador/importador de paquetes de mapas. Esta entrega no descarga mapas ni incluye datos cartográficos de México. Respeta las licencias y atribuciones de la fuente cartográfica. No descargues masivamente teselas de servidores públicos que prohíban ese uso.

## Enrutamiento offline: estado y datos necesarios
`RouteCalculator` implementa A* sobre un grafo vial en memoria. Para navegación real, el siguiente paso de integración es importar una extracción OSM de la región a tablas Room (nodos, aristas, restricciones, sentidos y accesibilidad por modo) y conectar el repositorio con el ViewModel.

No se inventan rutas cuando no hay grafo: la aplicación indica que no hay ruta activa. Los cinco modos están definidos, pero la calidad del cálculo depende de que los datos importados tengan restricciones reales:
- Auto/moto: sentidos, accesos, restricciones vehiculares.
- Bicicleta: vías permitidas y restricciones ciclistas.
- A pie: senderos y accesos peatonales.
- Transporte público: GTFS local (rutas, paradas, calendarios y horarios). No basta con una red vial OSM.

## GPS y ejecución en segundo plano
El rastreador incluido usa `LocationManager` mientras la Activity está activa. Android puede limitar la ubicación al pasar a segundo plano. Para rastreo continuo con pantalla apagada se debe añadir un servicio foreground de ubicación, notificación persistente y flujo de permisos de Android 14; no se declara como implementado en esta primera base.

## Voz
- TTS usa el idioma español de México y solicita Audio Focus transitorio con ducking.
- El sistema puede no tener una voz `es-MX` instalada.
- STT prefiere el reconocedor en dispositivo si está disponible. Si no existe, Android puede usar un servicio externo; por tanto, no se garantiza STT offline en todos los equipos.
- El permiso `RECORD_AUDIO` debe solicitarse en tiempo de ejecución antes de iniciar STT.

## Seguridad y privacidad
No se solicita permiso INTERNET. Los mapas y datos deben importarse por el usuario desde un medio local. La ubicación permanece en el dispositivo.

## Alcance de esta entrega
Esta es una base compilable y extensible, no un navegador listo para producción. Antes de trabajo de campo crítico se requiere:
1. Probar compilación en el CI del repositorio.
2. Añadir importador OSM/GTFS y persistencia del grafo.
3. Añadir seguimiento de ruta, ajuste a carretera, detección de desvíos y recálculo.
4. Añadir servicio foreground para navegación con pantalla apagada.
5. Añadir descarga/importación validada de paquetes de mapas.
6. Probar GPS, mapas, permisos, voz y consumo en el Galaxy A06 real.
