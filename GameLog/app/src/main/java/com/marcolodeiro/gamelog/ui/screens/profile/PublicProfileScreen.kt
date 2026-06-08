package com.marcolodeiro.gamelog.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.data.model.User
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.PublicProfileState
import com.marcolodeiro.gamelog.viewmodel.PublicProfileViewModel

@Composable
fun PublicProfileScreen(
    uid: String,
    onBack: () -> Unit,
    viewModel: PublicProfileViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val isFollowing by viewModel.isFollowing.collectAsState()

    LaunchedEffect(uid) {
        viewModel.loadProfile(uid)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        when (val currentState = state) {
            is PublicProfileState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonBlue)
                }
            }

            is PublicProfileState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentState.message, color = TextSecondary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onBack) { Text("Volver") }
                    }
                }
            }

            is PublicProfileState.Success -> {
                PublicProfileContent(
                    user = currentState.user,
                    library = currentState.library,
                    isFollowing = isFollowing,
                    onBack = onBack,
                    onFollowClick = { viewModel.toggleFollow(uid) }
                )
            }
        }
    }
}

@Composable
fun PublicProfileContent(
    user: User,
    library: List<GameLibraryEntry>,
    isFollowing: Boolean,
    onBack: () -> Unit,
    onFollowClick: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {

        // ── CABECERA con gradiente ────────────────────────────────────────
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(NeonBlue.copy(alpha = 0.3f), DarkBackground)
                        )
                    )
            ) {
                // Botón volver
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.padding(8.dp).align(Alignment.TopStart)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = Color.White)
                }

                // Foto, nombre y botón seguir
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (user.photoUrl.isNotBlank()) {
                        AsyncImage(
                            model = user.photoUrl,
                            contentDescription = user.displayName,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(NeonBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                user.displayName.firstOrNull()?.toString() ?: "G",
                                color = Color.White,
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = user.displayName,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Botón seguir / dejar de seguir
                    Button(
                        onClick = onFollowClick,
                        shape = RoundedCornerShape(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isFollowing) SurfaceCard else NeonBlue
                        ),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = if (isFollowing) "Siguiendo" else "Seguir",
                            fontWeight = FontWeight.Bold,
                            color = if (isFollowing) TextSecondary else Color.White
                        )
                    }
                }
            }
        }

        // ── ESTADÍSTICAS ──────────────────────────────────────────────────
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "ESTADÍSTICAS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val playing   = library.count { it.status == GameStatus.PLAYING.name }
                val completed = library.count { it.status == GameStatus.COMPLETED.name }
                val platinum  = library.count { it.status == GameStatus.PLATINUM.name }

                PublicStatCard(Modifier.weight(1f), playing.toString(), "Jugando", ColorPlaying)
                PublicStatCard(Modifier.weight(1f), completed.toString(), "Completados", NeonBlue)
                PublicStatCard(Modifier.weight(1f), platinum.toString(), "Platinados", ColorPlatinum)
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // ── BIBLIOTECA PÚBLICA ────────────────────────────────────────────
        item {
            Text(
                "BIBLIOTECA",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (library.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Este usuario no tiene juegos en su biblioteca", color = TextSecondary, fontSize = 14.sp)
                }
            }
        } else {
            items(library) { entry ->
                PublicLibraryCard(entry = entry, modifier = Modifier.padding(horizontal = 24.dp))
                Spacer(modifier = Modifier.height(10.dp))
            }
        }

        item { Spacer(modifier = Modifier.height(32.dp)) }
    }
}

// Tarjeta de estadística del perfil público
@Composable
fun PublicStatCard(modifier: Modifier, number: String, label: String, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(number, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(label, fontSize = 11.sp, color = TextSecondary)
        }
    }
}

// Tarjeta de juego en la biblioteca pública
@Composable
fun PublicLibraryCard(entry: GameLibraryEntry, modifier: Modifier = Modifier) {
    val statusColor = when (entry.status) {
        "PLAYING"   -> ColorPlaying
        "COMPLETED" -> NeonBlue
        "PLATINUM"  -> ColorPlatinum
        "PENDING"   -> ColorPending
        "ABANDONED" -> ColorAbandoned
        "WISHLIST"  -> ColorWishlist
        else        -> TextSecondary
    }
    val statusLabel = GameStatus.entries.find { it.name == entry.status }?.label ?: entry.status

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = entry.gameCover,
                contentDescription = entry.gameName,
                modifier = Modifier
                    .size(52.dp, 68.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    entry.gameName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            entry.gameRating?.let {
                Text(
                    "⭐ ${String.format("%.0f", it)}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}