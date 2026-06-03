package com.marcolodeiro.gamelog.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.NewReleases
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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

@Composable
fun FeedScreen(
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Actividad", "Opiniones", "Noticias")

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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Tablón Gamer",
                fontSize = 24.sp,
                fontWeight = FontWeight.Black,
                color = TextPrimary
            )
        }

        // ── PESTAÑAS (TAB ROW) NATIVAS ────────────────────────────────────
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = SurfaceDark,
            contentColor = NeonBlue,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NeonBlue
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp,
                            color = if (selectedTab == index) TextPrimary else TextSecondary
                        )
                    }
                )
            }
        }

        // ── CONTENIDO DINÁMICO SEGÚN PESTAÑA ──────────────────────────────
        when (selectedTab) {
            0 -> ActivityTabContent(state, viewModel)
            1 -> OpinionsTabContent() // Foro / Reviews detalladas
            2 -> NewsTabContent()     // Anuncios oficiales de la industria
        }
    }
}

@Composable
fun ActivityTabContent(state: FeedState, viewModel: FeedViewModel) {
    when (val currentState = state) {
        is FeedState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
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
                        colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                    ) { Text("Reintentar") }
                }
            }
        }
        is FeedState.Success -> {
            if (currentState.items.isEmpty()) {
                EmptyStateView(icon = "👥", text = "Sigue a otros gamers para ver su actividad")
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

@Composable
fun OpinionsTabContent() {
    // Simulamos un tablón comunitario enriquecido
    val mockOpinions = listOf(
        Pair("Geralt_Es", "¡Acabo de terminar The Witcher 3 por tercera vez! Sigue siendo una obra maestra indiscutible, la narrativa de los DLCs destroza al 90% de juegos actuales... 🎭 #Review"),
        Pair("Kratos99", "Pregunta seria: ¿Creéis que el nuevo DLC merece la pena o es puro reciclaje? Estoy dudando si pillarlo en las rebajas de verano 🤔 #Debate")
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mockOpinions) { (user, review) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(32.dp).clip(CircleShape).background(NeonBlue), contentAlignment = Alignment.Center) {
                            Text(user.first().toString(), color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(user, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(review, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        IconButton(onClick = {}) { Row { Icon(Icons.Default.ThumbUp, "", tint = TextSecondary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("12", color = TextSecondary, fontSize = 12.sp) } }
                        IconButton(onClick = {}) { Row { Icon(Icons.Default.Comment, "", tint = TextSecondary, modifier = Modifier.size(18.dp)); Spacer(modifier = Modifier.width(4.dp)); Text("4", color = TextSecondary, fontSize = 12.sp) } }
                    }
                }
            }
        }
    }
}

@Composable
fun NewsTabContent() {
    // Tablón informativo simulado
    val mockNews = listOf(
        Pair("Anuncio Oficial del E3 2026", "Se confirman las fechas del evento más esperado del año. Nuevos tráilers exclusivos en camino."),
        Pair("Parche de optimización Next-Gen", "La última actualización mejora drásticamente la tasa de frames en consolas portátiles.")
    )

    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(mockNews) { (title, content) ->
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.NewReleases, contentDescription = null, tint = ColorPlaying, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(content, color = TextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: String, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 48.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, color = TextSecondary, fontSize = 14.sp)
        }
    }
}

@Composable
fun FeedItemCard(item: FeedItem) {
    val statusLabel = GameStatus.entries.find { it.name == item.status }?.label ?: item.status
    val statusColor = when (item.status) {
        "PLAYING"   -> Color(0xFF4CAF50)
        "COMPLETED" -> NeonBlue
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
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                if (item.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = item.userPhoto,
                        contentDescription = item.userName,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(NeonBlue), contentAlignment = Alignment.Center) {
                        Text(text = item.userName.firstOrNull()?.toString() ?: "G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(text = item.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                Text(text = SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(item.timestamp)), color = TextSecondary, fontSize = 11.sp)
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.gameCover.isNotBlank()) {
                    AsyncImage(
                        model = item.gameCover,
                        contentDescription = item.gameName,
                        modifier = Modifier.size(56.dp, 72.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(modifier = Modifier.size(56.dp, 72.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF2A2A2A)), contentAlignment = Alignment.Center) {
                        Text("🎮", fontSize = 24.sp)
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = item.gameName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(shape = RoundedCornerShape(6.dp), color = statusColor.copy(alpha = 0.15f)) {
                        Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(text = statusEmoji, fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = statusLabel, color = statusColor, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}