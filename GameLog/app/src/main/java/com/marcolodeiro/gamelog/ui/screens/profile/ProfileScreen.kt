package com.marcolodeiro.gamelog.ui.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.ProfileStats
import com.marcolodeiro.gamelog.viewmodel.ProfileViewModel
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.PaddingValues
import com.marcolodeiro.gamelog.ui.screens.home.ActiveGameCard
import com.marcolodeiro.gamelog.viewmodel.HomeViewModel

// Pantalla de perfil del usuario con estadísticas y opción de cerrar sesión
@Composable
fun ProfileScreen(
    onSignOut: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val stats by viewModel.stats.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val user = viewModel.currentUser
    var showSignOutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .verticalScroll(rememberScrollState())
    ) {
        // ── CABECERA con foto y nombre ────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Foto de perfil
                if (user?.photoUrl != null) {
                    AsyncImage(
                        model = user.photoUrl,
                        contentDescription = "Foto de perfil",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(AccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = user?.displayName?.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Nombre del usuario
                Text(
                    text = user?.displayName ?: "Gamer",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                // Email
                Text(
                    text = user?.email ?: "",
                    fontSize = 14.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Badge de gamer con total de juegos
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = AccentRed.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = "🎮 ${stats.totalGames} juegos en biblioteca",
                        color = AccentRed,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AccentRed)
            }
        } else {
            // ── ESTADÍSTICAS por estado ───────────────────────────────────
            Text(
                text = "Estadísticas",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Grid de estadísticas 2x3
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "🎮",
                        count = stats.playing,
                        label = "Jugando",
                        color = Color(0xFF4CAF50)
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "✅",
                        count = stats.completed,
                        label = "Completados",
                        color = Color(0xFF2196F3)
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "🏆",
                        count = stats.platinum,
                        label = "Platinados",
                        color = Color(0xFF9C27B0)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "📋",
                        count = stats.pending,
                        label = "Pendientes",
                        color = Color(0xFFFF9800)
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "⏸️",
                        count = stats.abandoned,
                        label = "Abandonados",
                        color = Color(0xFFF44336)
                    )
                    StatItem(
                        modifier = Modifier.weight(1f),
                        emoji = "💭",
                        count = stats.wishlist,
                        label = "Deseados",
                        color = Color(0xFF00BCD4)
                    )
                }
            }


            Spacer(modifier = Modifier.height(24.dp))

// ── CONTINUAR JUGANDO ─────────────────────────────────────────
            val activeGames by homeViewModel.activeGames.collectAsState()

            Text(
                "CONTINUAR JUGANDO",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            if (activeGames.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Text(
                        "No tienes juegos en progreso actualmente",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(activeGames) { entry ->
                        ActiveGameCard(
                            title = entry.gameName,
                            coverUrl = entry.gameCover,
                            onClick = {}
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))





            // ── BOTÓN CERRAR SESIÓN ───────────────────────────────────────
            OutlinedButton(
                onClick = { showSignOutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentRed),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentRed)
            ) {
                Text(
                    text = "Cerrar sesión",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de confirmación de cierre de sesión
    if (showSignOutDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Cerrar sesión", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text("¿Seguro que quieres cerrar sesión?", color = TextSecondary)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.signOut()
                    onSignOut()
                }) {
                    Text("Cerrar sesión", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showSignOutDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}

// Tarjeta individual de estadística
@Composable
fun StatItem(
    modifier: Modifier = Modifier,
    emoji: String,
    count: Int,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}