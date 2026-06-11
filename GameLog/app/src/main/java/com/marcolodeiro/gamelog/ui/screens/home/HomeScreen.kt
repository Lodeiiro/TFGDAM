package com.marcolodeiro.gamelog.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.AuthViewModel
import com.marcolodeiro.gamelog.viewmodel.HomeViewModel
import com.marcolodeiro.gamelog.viewmodel.GameRecommendation

@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToChatbot: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val user = authViewModel.currentUser
    val playingCount by homeViewModel.playing.collectAsState()
    val completedCount by homeViewModel.completed.collectAsState()
    val pendingCount by homeViewModel.pending.collectAsState()
    val recommendations by homeViewModel.recommendations.collectAsState()
    val recommendationsLoading by homeViewModel.recommendationsLoading.collectAsState()
    val activeGames by homeViewModel.activeGames.collectAsState()





    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // ── CABECERA ──────────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(Brush.verticalGradient(colors = listOf(NeonBlue.copy(alpha = 0.25f), DarkBackground)))
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("PANEL DE CONTROL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = NeonBlue, letterSpacing = 2.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Hola, ${user?.displayName?.split(" ")?.firstOrNull() ?: "Gamer"}",
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(Brush.linearGradient(colors = listOf(NeonBlue, NeonBlueLight))).padding(2.dp).clickable { onNavigateToProfile() }) {
                        if (user?.photoUrl != null) {
                            AsyncImage(model = user.photoUrl, contentDescription = null, modifier = Modifier.fillMaxSize().clip(CircleShape), contentScale = ContentScale.Crop)
                        } else {
                            Box(modifier = Modifier.fillMaxSize().background(SurfaceCard), contentAlignment = Alignment.Center) {
                                Text(user?.displayName?.firstOrNull()?.toString() ?: "G", color = TextPrimary, fontWeight = FontWeight.Black, fontSize = 20.sp)
                            }
                        }
                    }
                }
            }

            // ── ESTADÍSTICAS COMPACTAS ────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CompactStatCard(modifier = Modifier.weight(1f), number = playingCount.toString(), label = "En curso", color = ColorPlaying)
                CompactStatCard(modifier = Modifier.weight(1f), number = completedCount.toString(), label = "Terminados", color = NeonBlue)
                CompactStatCard(modifier = Modifier.weight(1f), number = pendingCount.toString(), label = "Pendientes", color = ColorPending)
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── RECOMENDACIONES IA ────────────────────────────────────────
            Text(
                "ESTO PODRÍA INTERESARTE",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))


            if (recommendationsLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = NeonBlue, modifier = Modifier.size(32.dp))
                }
            } else if (recommendations.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Text(
                        "Añade juegos a tu biblioteca para recibir recomendaciones personalizadas",
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
                    items(recommendations) { rec ->
                        RecommendationCard(recommendation = rec)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Spacer(modifier = Modifier.height(28.dp))

            // ── CORREGIDO: BANNER ASISTENTE IA ENLAZADO AL CHATBOT ─────────
            Text("RECOMENDACIÓN INTELIGENTE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                border = BorderStroke(1.dp, Brush.linearGradient(listOf(NeonBlue.copy(alpha = 0.5f), Color.Transparent)))
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🤖", fontSize = 36.sp)
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("¿Indeciso sobre qué jugar?", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Deja que el chatbot analice tus gustos y elija tu próximo desafío.", color = TextSecondary, fontSize = 12.sp)
                    }
                    Button(
                        onClick = onNavigateToChatbot, // 👈 CORREGIDO: Ahora va directo al chatbot
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Preguntar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // ── ACCESOS DIRECTOS ──────────────────────────────────────────
            Text("ACCESOS DIRECTOS", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextSecondary, letterSpacing = 1.5.sp, modifier = Modifier.padding(horizontal = 24.dp))
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HomeMenuButton(modifier = Modifier.weight(1f), icon = Icons.Default.Search, label = "Explorar", onClick = onNavigateToExplore)
                HomeMenuButton(modifier = Modifier.weight(1f), icon = Icons.Default.LibraryBooks, label = "Biblioteca", onClick = onNavigateToLibrary)
                HomeMenuButton(modifier = Modifier.weight(1f), icon = Icons.Default.Person, label = "Mi Perfil", onClick = onNavigateToProfile)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun CompactStatCard(modifier: Modifier = Modifier, number: String, label: String, color: Color) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = SurfaceCard)) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = number, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = label, fontSize = 11.sp, color = TextSecondary, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


@Composable
fun ActiveGameCard(title: String, coverUrl: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.width(130.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column {
            if (coverUrl.isNotBlank()) {
                AsyncImage(
                    model = coverUrl,
                    contentDescription = title,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(140.dp).background(SurfaceCard), contentAlignment = Alignment.Center) {
                    Text("🎮", fontSize = 32.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(progress = { 0.35f }, modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape), color = ColorPlaying, trackColor = Color(0xFF2A2A2A))
            }
        }
    }
}

@Composable
fun HomeMenuButton(modifier: Modifier = Modifier, icon: ImageVector, label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = label, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Tarjeta de recomendación de juego con IA

@Composable
fun RecommendationCard(recommendation: GameRecommendation) {
    Card(
        modifier = Modifier.width(130.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column {
            if (recommendation.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = recommendation.coverUrl,
                    contentDescription = recommendation.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .background(SurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎮", fontSize = 36.sp)
                }
            }
            Column(modifier = Modifier.padding(10.dp)) {
                Text(
                    text = recommendation.name,
                    color = TextPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

