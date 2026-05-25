package com.marcolodeiro.gamelog.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
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
import com.marcolodeiro.gamelog.data.model.GameLibraryEntry
import com.marcolodeiro.gamelog.ui.screens.detail.GameStatus
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.LibraryState
import com.marcolodeiro.gamelog.viewmodel.LibraryViewModel

// Pantalla de biblioteca personal del usuario
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val activeFilter by viewModel.activeFilter.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── CABECERA ──────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Text(
                text = "Mi Biblioteca",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Filtros por estado en fila horizontal con scroll
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Opción "Todos"
                item {
                    FilterChip(
                        selected = activeFilter == null,
                        onClick = { viewModel.setFilter(null) },
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = TextSecondary
                        )
                    )
                }
                // Un chip por cada estado posible
                items(GameStatus.entries) { status ->
                    FilterChip(
                        selected = activeFilter == status,
                        onClick = { viewModel.setFilter(status) },
                        label = { Text(status.label) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentRed,
                            selectedLabelColor = Color.White,
                            containerColor = Color(0xFF2A2A2A),
                            labelColor = TextSecondary
                        )
                    )
                }
            }
        }

        // ── CONTENIDO según el estado ─────────────────────────────────────
        when (val currentState = state) {
            is LibraryState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is LibraryState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("😕", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(currentState.message, color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.loadLibrary() },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentRed)
                        ) { Text("Reintentar") }
                    }
                }
            }

            is LibraryState.Success -> {
                // Filtramos los juegos según el filtro activo
                val filteredGames = if (activeFilter == null) {
                    currentState.games
                } else {
                    currentState.games.filter { it.status == activeFilter!!.name }
                }

                if (filteredGames.isEmpty()) {
                    // Biblioteca vacía
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🎮", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = if (activeFilter == null) "Tu biblioteca está vacía" else "No tienes juegos en este estado",
                                color = TextSecondary,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    // Lista de juegos
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredGames) { entry ->
                            LibraryGameCard(
                                entry = entry,
                                onDelete = { viewModel.removeGame(entry.gameId) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de juego en la biblioteca con portada, nombre, estado y botón eliminar
@Composable
fun LibraryGameCard(
    entry: GameLibraryEntry,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Portada del juego en miniatura
            AsyncImage(
                model = entry.gameCover,
                contentDescription = entry.gameName,
                modifier = Modifier
                    .size(70.dp, 90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre, estado y puntuación
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.gameName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                // Badge de estado con color según el estado
                val statusColor = when (entry.status) {
                    "PLAYING"   -> Color(0xFF4CAF50)
                    "COMPLETED" -> Color(0xFF2196F3)
                    "PLATINUM"  -> Color(0xFF9C27B0)
                    "PENDING"   -> Color(0xFFFF9800)
                    "ABANDONED" -> Color(0xFFF44336)
                    "WISHLIST"  -> Color(0xFF00BCD4)
                    else        -> TextSecondary
                }
                val statusLabel = GameStatus.entries
                    .find { it.name == entry.status }?.label ?: entry.status

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = statusColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = statusLabel,
                        color = statusColor,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }

                // Puntuación si existe
                entry.gameRating?.let { rating ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "⭐ ${String.format("%.1f", rating)}/100",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Botón eliminar
            IconButton(onClick = { showDeleteDialog = true }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = Color(0xFF666666)
                )
            }
        }
    }

    // Diálogo de confirmación para eliminar
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            containerColor = SurfaceDark,
            title = {
                Text("Eliminar juego", color = TextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    "¿Quieres eliminar ${entry.gameName} de tu biblioteca?",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onDelete()
                    showDeleteDialog = false
                }) {
                    Text("Eliminar", color = AccentRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = TextSecondary)
                }
            }
        )
    }
}