package com.marcolodeiro.gamelog.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marcolodeiro.gamelog.data.model.Game
import com.marcolodeiro.gamelog.data.model.getImageUrl
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.GameDetailViewModel
import com.marcolodeiro.gamelog.viewmodel.LibraryActionState

// Estados posibles para añadir un juego a la biblioteca
enum class GameStatus(val label: String) {
    PLAYING("Jugando"),
    COMPLETED("Completado"),
    PLATINUM("Platinado"),
    PENDING("Pendiente"),
    ABANDONED("Abandonado"),
    WISHLIST("Quiero jugarlo")
}

@Composable
fun GameDetailScreen(
    game: Game,
    onBack: () -> Unit,
    viewModel: GameDetailViewModel = hiltViewModel()
) {
    val actionState by viewModel.actionState.collectAsState()
    val currentStatus by viewModel.currentStatus.collectAsState()
    var showStatusDialog by remember { mutableStateOf(false) }

    // Al entrar comprobamos si el juego ya está en la biblioteca
    LaunchedEffect(game.id) {
        viewModel.checkGameStatus(game.id)
    }

    // Mostramos snackbar de éxito o error
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(actionState) {
        when (actionState) {
            is LibraryActionState.Success ->
                snackbarHostState.showSnackbar("Juego añadido a tu biblioteca")
            is LibraryActionState.Error ->
                snackbarHostState.showSnackbar((actionState as LibraryActionState.Error).message)
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBackground
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

                // ── CABECERA con portada a pantalla completa ──────────────
                Box(modifier = Modifier.fillMaxWidth().height(300.dp)) {

                    if (game.cover != null) {
                        AsyncImage(
                            model = game.cover.getImageUrl(),
                            contentDescription = game.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize().background(SurfaceDark),
                            contentAlignment = Alignment.Center
                        ) { Text("🎮", fontSize = 72.sp) }
                    }

                    // Degradado sobre la imagen para legibilidad
                    Box(
                        modifier = Modifier.fillMaxSize().background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, DarkBackground),
                                startY = 100f
                            )
                        )
                    )

                    // Botón volver
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                    ) {
                        Icon(Icons.Default.ArrowBack, "Volver", tint = Color.White)
                    }

                    // Nombre y géneros encima de la portada
                    Column(modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)) {
                        Text(
                            text = game.name,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        game.genres?.let { genres ->
                            Text(
                                text = genres.joinToString(", ") { it.name },
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                // ── CONTENIDO ─────────────────────────────────────────────
                Column(modifier = Modifier.padding(16.dp)) {

                    // Puntuación y plataformas
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        game.rating?.let { rating ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Star, null,
                                    tint = Color(0xFFFFD700),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = String.format("%.1f", rating),
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text("/100", color = TextSecondary, fontSize = 14.sp)
                            }
                        }
                        game.platforms?.take(3)?.let { platforms ->
                            Text(
                                text = platforms.joinToString(" · ") { it.name },
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Botón añadir a biblioteca
                    Button(
                        onClick = { showStatusDialog = true },
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (currentStatus != null) Color(0xFF1D9E75) else AccentRed
                        ),
                        enabled = actionState !is LibraryActionState.Loading
                    ) {
                        if (actionState is LibraryActionState.Loading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = if (currentStatus != null) "✓ ${currentStatus!!.label}" else "Añadir a mi biblioteca",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Descripción
                    if (game.summary.isNotBlank()) {
                        Text(
                            text = "Descripción",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = game.summary,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            lineHeight = 22.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }

            // ── DIÁLOGO selector de estado ────────────────────────────────
            if (showStatusDialog) {
                AlertDialog(
                    onDismissRequest = { showStatusDialog = false },
                    containerColor = SurfaceDark,
                    title = {
                        Text(
                            "¿En qué estado está?",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            GameStatus.entries.forEach { status ->
                                TextButton(
                                    onClick = {
                                        viewModel.addToLibrary(game, status)
                                        showStatusDialog = false
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = status.label,
                                        color = if (currentStatus == status) AccentRed else TextPrimary,
                                        fontSize = 16.sp,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    },
                    confirmButton = {}
                )
            }
        }
    }
}