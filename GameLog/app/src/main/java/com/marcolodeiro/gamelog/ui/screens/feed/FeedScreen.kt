package com.marcolodeiro.gamelog.ui.screens.feed

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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

        // ── PESTAÑAS (TAB ROW) ────────────────────────────────────────────
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

        // ── CONTENIDO REAL SEGÚN PESTAÑA ──────────────────────────────────
        when (selectedTab) {
            0 -> ActivityTabContent(state, viewModel, onlyReviews = false)
            1 -> ActivityTabContent(state, viewModel, onlyReviews = true) // 👈 CORREGIDO: Mismo estado, pero filtrando reviews reales
            2 -> NewsTabContent() // Noticias del sector
        }
    }
}

@Composable
fun ActivityTabContent(state: FeedState, viewModel: FeedViewModel, onlyReviews: Boolean) {
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
                    Button(onClick = { viewModel.loadFeed() }, colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)) { Text("Reintentar") }
                }
            }
        }
        is FeedState.Success -> {
            // 👈 CORREGIDO: Filtramos los items de la base de datos si estamos en la pestaña opiniones
            val itemsToShow = if (onlyReviews) {
                currentState.items.filter { it.status == "COMPLETED" || it.status == "PLATINUM" }
            } else {
                currentState.items
            }

            if (itemsToShow.isEmpty()) {
                val emptyText = if (onlyReviews) "Aún no hay opiniones o análisis de juegos publicados." else "Sigue a otros gamers para ver su actividad."
                EmptyStateView(icon = if (onlyReviews) "📝" else "👥", text = emptyText)
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsToShow) { item ->
                        FeedItemCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsTabContent() {
    // Aquí puedes enlazarlo en el futuro con un NewsViewModel, de momento queda estructurado de forma limpia:
    val mockNews = listOf(
        Pair("Próximos lanzamientos Junio 2026", "Repasamos los títulos más potentes que llegan a las tiendas esta semana."),
        Pair("Nueva actualización de Firmware", "Mejoras de rendimiento de red para optimizar las partidas en la nube.")
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
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(content, color = TextSecondary, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
            }
        }
    }
}

@Composable
fun EmptyStateView(icon: String, text: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(icon, fontSize = 44.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = text, color = TextSecondary, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 32.dp), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
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
                    AsyncImage(model = item.userPhoto, contentDescription = item.userName, modifier = Modifier.size(36.dp).clip(CircleShape), contentScale = ContentScale.Crop)
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
                    AsyncImage(model = item.gameCover, contentDescription = item.gameName, modifier = Modifier.size(56.dp, 72.dp).clip(RoundedCornerShape(8.dp)), contentScale = ContentScale.Crop)
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