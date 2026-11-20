package com.example.dialogcompose

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.dialogcompose.ui.theme.DialogComposeTheme

// Modelo de ubicacion geografica para mostrar en el mapa y dentro del dialogo
data class PlaceLocation(
    val id: Int,
    val name: String,
    val subtitle: String,
    val lat: Double,
    val lng: Double
) {
    val coordinates: String
        get() = "$lat, $lng"
}

// Ubicaciones predefinidas iniciando con la Universidad Politecnica de Pachuca (UPP)
val defaultPlaces = listOf(
    PlaceLocation(
        1,
        "UPP - Pachuca",
        "Universidad Politécnica de Pachuca, Zempoala, Hgo.",
        19.9798144,
        -98.6846703
    ),
    PlaceLocation(2, "CDMX - Zócalo", "Centro Histórico, Ciudad de México", 19.4326, -99.1332),
    PlaceLocation(3, "Guadalajara", "Centro Histórico, Jalisco", 20.6767, -103.3475),
    PlaceLocation(4, "Monterrey", "Macroplaza, Nuevo León", 25.6691, -100.3099)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DialogComposeTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    DialogMapsScreen(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun DialogMapsScreen(modifier: Modifier = Modifier) {
    // Estado principal que controla la visibilidad del AlertDialog (practica con states)
    var show by rememberSaveable { mutableStateOf(false) }
    var selectedPlaceId by rememberSaveable { mutableIntStateOf(defaultPlaces.first().id) }
    var confirmCount by rememberSaveable { mutableIntStateOf(0) }
    var statusMessage by rememberSaveable {
        mutableStateOf("Selecciona una ubicación en el mapa o presiona el botón para abrir el diálogo.")
    }

    val selectedPlace = defaultPlaces.find { it.id == selectedPlaceId } ?: defaultPlaces.first()
    val configuredApiKey = stringResource(id = R.string.google_maps_key)
    val hasCustomKey = configuredApiKey.isNotBlank() &&
        configuredApiKey != "TU_GOOGLE_MAPS_API_KEY_AQUI"

    Surface(
        modifier = modifier.fillMaxSize(),
        color = Color(0xFFF3F6FC)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
// Simplified header with logo and title
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF4A148C))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_upp),
                    contentDescription = "Escudo UPP",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(48.dp)
                )
                Text(
                    text = "DialogCompose – UPP",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                // API key status badge
                Surface(
                    color = if (hasCustomKey) Color(0xFF00C853) else Color(0xFFFFAB00),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = if (hasCustomKey) "API Key Activa" else "Mapa Integrado",
                        color = Color(0xFF0F172A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            // Selector rapido de ubicaciones para actualizar el mapa dinamicamente
            Text(
                text = "📍 Ubicaciones disponibles",
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = Color(0xFF1E293B)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                defaultPlaces.forEach { place ->
                    val isSelected = place.id == selectedPlace.id
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) Color(0xFF4A148C) else Color.White,
                        shadowElevation = if (isSelected) 4.dp else 1.dp,
                        modifier = Modifier.clickable {
                            selectedPlaceId = place.id
                            statusMessage = "Mapa actualizado a: ${place.name}"
                        }
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = place.name,
                                color = if (isSelected) Color.White else Color(0xFF1E293B),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = place.coordinates,
                                color = if (isSelected) Color.White.copy(alpha = 0.85f) else Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }

            // Tarjeta con vista interactiva de Google Maps centrada en la UPP por defecto
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF8FAFC))
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = selectedPlace.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = selectedPlace.subtitle,
                                fontSize = 12.sp,
                                color = Color(0xFF475569)
                            )
                        }
                        Text(
                            text = "Google Maps",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF1A73E8)
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize()) {
                        GoogleMapCardView(
                            place = selectedPlace,
                            apiKey = configuredApiKey,
                            hasCustomKey = hasCustomKey
                        )
                    }
                }
            }

            // Tarjeta principal de accion para abrir el AlertDialog (Box + Button de la practica)
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Control de AlertDialog con Estados",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFF1E293B)
                    )
                    Text(
                        text = statusMessage,
                        fontSize = 13.sp,
                        color = Color(0xFF475569),
                        textAlign = TextAlign.Center
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = Color(0xFFE8F0FE),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Estado show: $show",
                                color = Color(0xFF1A73E8),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                        Surface(
                            color = Color(0xFFE6F4EA),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Confirmaciones (Logcat): $confirmCount",
                                color = Color(0xFF137333),
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // Box centrando el boton principal tal como en la practica original
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Button(
                            onClick = { show = true },
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4A148C)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Mostrar Diálogo (${selectedPlace.name})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Invocacion del Composable MyDialog pasando el estado `show` y las funciones lambda
            MyDialog(
                show = show,
                onDismiss = {
                    show = false
                    statusMessage = "Diálogo cerrado (onDismiss → show = false)"
                },
                onConfirm = {
                    Log.i("aris", "click - Ubicacion confirmada: ${selectedPlace.name}")
                    confirmCount++
                    show = false
                    statusMessage = "¡Confirmado en Logcat ('aris': 'click') para ${selectedPlace.name}!"
                },
                selectedPlace = selectedPlace
            )
        }
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GoogleMapCardView(
    place: PlaceLocation,
    apiKey: String,
    hasCustomKey: Boolean
) {
    val mapUrl = if (hasCustomKey && apiKey.startsWith("AIza")) {
        "https://www.google.com/maps/embed/v1/place?key=$apiKey&q=${place.lat},${place.lng}&zoom=16"
    } else {
        "https://maps.google.com/maps?q=${place.lat},${place.lng}&z=16&output=embed"
    }

    val htmlContent = """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background: #e5e3df; }
                iframe { width: 100%; height: 100%; border: 0; }
            </style>
        </head>
        <body>
            <iframe src="$mapUrl" allowfullscreen loading="lazy"></iframe>
        </body>
        </html>
    """.trimIndent()

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                "https://maps.google.com",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        }
    )
}

@Composable
fun MyDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    selectedPlace: PlaceLocation = defaultPlaces.first()
) {
    if (show) {
        AlertDialog(
            onDismissRequest = { onDismiss() },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White,
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3E8FF)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.logo_upp),
                        contentDescription = "Escudo UPP",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                    )
                }
            },
            title = {
                Text(
                    text = "Suscríbete",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color(0xFF0F172A),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Esto es el body de mi diálogo implementado desde cero con estados y funciones lambda en Jetpack Compose.",
                        fontSize = 14.sp,
                        color = Color(0xFF334155)
                    )
                    Surface(
                        color = Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "📍 Ubicación en Google Maps:",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF4A148C)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${selectedPlace.name} (${selectedPlace.subtitle})",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0F172A)
                            )
                            Text(
                                text = "Coordenadas: ${selectedPlace.coordinates}",
                                fontSize = 12.sp,
                                color = Color(0xFF64748B)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { onConfirm() },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A148C))
                ) {
                    Text(text = "ConfirmButton", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { onDismiss() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(text = "DismissButton", color = Color(0xFF475569))
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DialogMapsScreenPreview() {
    DialogComposeTheme {
        DialogMapsScreen()
    }
}
