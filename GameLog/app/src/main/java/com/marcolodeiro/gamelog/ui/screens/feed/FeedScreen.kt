package com.marcolodeiro.gamelog.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.marcolodeiro.gamelog.data.model.FeedItem
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.FeedState
import com.marcolodeiro.gamelog.viewmodel.FeedViewModel
import java.text.SimpleDateFormat
import java.util.*

// Pantalla de feed con la actividad de los usuarios que seguimos
@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── CABECERA ──────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Text(
                text = "Feed",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // ── CONTENIDO ─────────────────────────────────────────────────────
        when (val currentState = state) {
            is FeedState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is FeedState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentState.message, color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadFeed() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                        ) { Text("Reintentar") }
                    }
                }
            }

            is FeedState.Success -> {
                if (currentState.items.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("👥", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sigue a otros gamers para ver su actividad",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentState.items) { item ->
                            FeedItemCard(item = item)
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta individual del feed
// Tarjeta individual del feed con diseño mejorado
@Composable
fun FeedItemCard(item: FeedItem) {
    val statusLabel = GameStatus.entries
        .find { it.name == item.status }?.label ?: item.status

    val statusColor = when (item.status) {
        "PLAYING"   -> Color(0xFF4CAF50)
        "COMPLETED" -> Color(0xFF2196F3)
        "PLATINUM"  -> Color(0xFF9C27B0)
        "PENDING"   -> Color(0xFFFF9800)
        "ABANDONED" -> Color(0xFFF44336)
        "WISHLIST"  -> Color(0xFF00BCD4)
        else        -> TextSecondary
    }

    val statusEmoji = when (item.status) {
        "PLAYING"   -> "🎮"
        "COMPLETED" -> "✅"
        "PLATINUM"  -> "🏆"
        "PENDING"   -> "📋"
        "ABANDONED" -> "⏸️"
        "WISHLIST"  -> "💭"
        else        -> "🎮"
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            // ── FILA SUPERIOR: foto + nombre + tiempo ─────────────────────
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Foto de perfil
                if (item.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = item.userPhoto,
                        contentDescription = item.userName,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(AccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = item.userName.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Nombre del usuario
                Text(
                    text = item.userName,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )

                // Timestamp
                Text(
                    text = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                        .format(Date(item.timestamp)),
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(12.dp))

            // ── FILA INFERIOR: portada + info del juego ───────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {

                // Portada del juego
                if (item.gameCover.isNotBlank()) {
                    AsyncImage(
                        model = item.gameCover,
                        contentDescription = item.gameName,
                        modifier = Modifier
                            .size(56.dp, 72.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(56.dp, 72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF2A2A2A)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎮", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Info del juego
                Column(modifier = Modifier.weight(1f)) {
                    // Nombre del juego
                    Text(
                        text = item.gameName,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 2,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Badge de estado
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusColor.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = statusEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = statusLabel,
                                color = statusColor,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
  }
