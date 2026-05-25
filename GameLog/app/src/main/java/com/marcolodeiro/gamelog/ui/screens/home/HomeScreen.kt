package com.marcolodeiro.gamelog.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.AuthViewModel
import com.marcolodeiro.gamelog.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    onNavigateToExplore: () -> Unit,
    onNavigateToLibrary: () -> Unit,
    onNavigateToProfile: () -> Unit,
    authViewModel: AuthViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel()
) {
    val user = authViewModel.currentUser
    val playing by homeViewModel.playing.collectAsState()
    val completed by homeViewModel.completed.collectAsState()
    val pending by homeViewModel.pending.collectAsState()

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
            // ── CABECERA con gradiente neón ───────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NeonBlue.copy(alpha = 0.3f),
                                DarkBackground
                            )
                        )
                    )
                    .padding(24.dp),
                contentAlignment = Alignment.BottomStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Bienvenido,",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            letterSpacing = 2.sp
                        )
                        Text(
                            text = user?.displayName?.split(" ")?.firstOrNull() ?: "Gamer",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                    }

                    // Foto de perfil con borde neón
                    Box {
                        if (user?.photoUrl != null) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(NeonBlue, NeonBlueLight)
                                        )
                                    )
                                    .padding(2.dp)
                            ) {
                                AsyncImage(
                                    model = user.photoUrl,
                                    contentDescription = "Foto de perfil",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(NeonBlue, NeonBlueLight)
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = user?.displayName?.firstOrNull()?.toString() ?: "G",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 22.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ── ACCESOS RÁPIDOS ───────────────────────────────────────────
            Text(
                text = "¿QUÉ QUIERES HACER?",
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Search,
                    title = "Explorar",
                    subtitle = "Descubre juegos",
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF0070F3), Color(0xFF00A8FF))
                    ),
                    onClick = onNavigateToExplore
                )
                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.LibraryBooks,
                    title = "Biblioteca",
                    subtitle = "Tus juegos",
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFF1DB954), Color(0xFF1ED760))
                    ),
                    onClick = onNavigateToLibrary
                )
                QuickAccessCard(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Default.Person,
                    title = "Perfil",
                    subtitle = "Tu cuenta",
                    gradient = Brush.linearGradient(
                        colors = listOf(Color(0xFFB3A0FF), Color(0xFF7B5FFF))
                    ),
                    onClick = onNavigateToProfile
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── ESTADÍSTICAS ──────────────────────────────────────────────
            Text(
                text = "TU ACTIVIDAD",
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(modifier = Modifier.weight(1f), number = playing.toString(), label = "Jugando", color = ColorPlaying)
                StatCard(modifier = Modifier.weight(1f), number = completed.toString(), label = "Completados", color = NeonBlue)
                StatCard(modifier = Modifier.weight(1f), number = pending.toString(), label = "Pendientes", color = ColorPending)
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── CALL TO ACTION ────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                NeonBlue.copy(alpha = 0.2f),
                                NeonBlueLight.copy(alpha = 0.05f)
                            )
                        )
                    )
            ) {
                // Borde neón sutil
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Transparent)
                        .padding(1.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🎮", fontSize = 44.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Empieza tu biblioteca",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Explora miles de juegos y añádelos a tu colección",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToExplore,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                            shape = RoundedCornerShape(50.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Explorar juegos",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// Tarjeta de acceso rápido con gradiente
@Composable
fun QuickAccessCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    subtitle: String,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gradient),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = TextSecondary
            )
        }
    }
}

// Tarjeta de estadística
@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    number: String,
    label: String,
    color: Color
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = number,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
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