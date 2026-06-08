package com.marcolodeiro.gamelog.ui.screens.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.ui.draw.clip
import com.marcolodeiro.gamelog.data.model.Review
import com.marcolodeiro.gamelog.viewmodel.ReviewViewModel
import com.marcolodeiro.gamelog.ui.theme.NeonBlue
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    viewModel: GameDetailViewModel = hiltViewModel(),
    reviewViewModel: ReviewViewModel = hiltViewModel()
) {
    val reviews by reviewViewModel.reviews.collectAsState()
    val userReview by reviewViewModel.userReview.collectAsState()
    val reviewState by reviewViewModel.state.collectAsState()
    var showReviewDialog by remember { mutableStateOf(false) }
    var reviewText by remember { mutableStateOf("") }
    var reviewRating by remember { mutableStateOf(5f) }

    LaunchedEffect(game.id) {
        viewModel.checkGameStatus(game.id)
        reviewViewModel.loadReviews(game.id)
    }

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

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── SECCIÓN DE RESEÑAS ────────────────────────────────────────
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Reseñas",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        TextButton(onClick = { showReviewDialog = true }) {
                            Text(
                                text = if (userReview != null) "Editar reseña" else "+ Añadir reseña",
                                color = NeonBlue,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reseña del usuario actual destacada
                    userReview?.let { review ->
                        ReviewCard(review = review, isOwn = true, onDelete = {
                            reviewViewModel.deleteReview(game.id)
                        })
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Reseñas de otros usuarios
                    reviews.filter { it.userId != userReview?.userId }.forEach { review ->
                        ReviewCard(review = review, isOwn = false, onDelete = {})
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (reviews.isEmpty() && userReview == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Sé el primero en reseñar este juego",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
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

            if (showReviewDialog) {
                AlertDialog(
                    onDismissRequest = { showReviewDialog = false },
                    containerColor = SurfaceDark,
                    title = {
                        Text(
                            "Tu reseña",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    text = {
                        Column {
                            // Puntuación
                            Text(
                                text = "Puntuación: ${reviewRating.toInt()}/10",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                            Slider(
                                value = reviewRating,
                                onValueChange = { reviewRating = it },
                                valueRange = 1f..10f,
                                steps = 8,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonBlue,
                                    activeTrackColor = NeonBlue
                                )
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            // Texto
                            OutlinedTextField(
                                value = reviewText,
                                onValueChange = { reviewText = it },
                                placeholder = { Text("Escribe tu opinión...", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth().height(120.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonBlue,
                                    unfocusedBorderColor = Color(0xFF333333),
                                    focusedTextColor = TextPrimary,
                                    unfocusedTextColor = TextPrimary,
                                    cursorColor = NeonBlue
                                ),
                                maxLines = 5
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                reviewViewModel.publishReview(game.id, game.name, reviewRating, reviewText)
                                showReviewDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            enabled = reviewText.isNotBlank()
                        ) {
                            Text("Publicar")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showReviewDialog = false }) {
                            Text("Cancelar", color = TextSecondary)
                        }
                    }
                )
            }

        }
    }
}

// Tarjeta de reseña individual
@Composable
fun ReviewCard(
    review: Review,
    isOwn: Boolean,
    onDelete: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwn) NeonBlue.copy(alpha = 0.1f) else SurfaceCard
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Foto de perfil
                if (review.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = review.userPhoto,
                        contentDescription = review.userName,
                        modifier = Modifier.size(32.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            review.userName.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = review.userName,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        if (isOwn) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonBlue.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    "Tú",
                                    color = NeonBlue,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(Date(review.timestamp)),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Puntuación
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "⭐ ${review.rating.toInt()}/10",
                        color = NeonBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Botón eliminar si es tuya
                if (isOwn) {
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = review.text,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}