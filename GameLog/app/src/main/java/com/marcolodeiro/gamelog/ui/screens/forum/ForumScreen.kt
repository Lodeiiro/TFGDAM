package com.marcolodeiro.gamelog.ui.screens.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
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
import com.marcolodeiro.gamelog.data.model.ForumThread
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.ForumState
import com.marcolodeiro.gamelog.viewmodel.ForumViewModel
import java.text.SimpleDateFormat
import java.util.*

// Lista de etiquetas disponibles para los hilos
val FORUM_TAGS = listOf("General", "RPG", "Acción", "Indie", "Retro", "Noticias", "Ayuda", "Recomendaciones")

@Composable
fun ForumScreen(
    onThreadClick: (ForumThread) -> Unit,
    viewModel: ForumViewModel = hiltViewModel()
) {
    val state by viewModel.forumState.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()
    var showCreateDialog by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize().background(DarkBackground)) {
        Column(modifier = Modifier.fillMaxSize()) {

            // ── CABECERA ──────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceDark)
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Foros",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = TextPrimary
                )
                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonBlue),
                    shape = RoundedCornerShape(10.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Nuevo hilo", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            // ── CONTENIDO ─────────────────────────────────────────────────
            when (val currentState = state) {
                is ForumState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = NeonBlue)
                    }
                }
                is ForumState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("😕", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(currentState.message, color = TextSecondary)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = { viewModel.loadThreads() },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
                            ) { Text("Reintentar") }
                        }
                    }
                }
                is ForumState.Success -> {
                    if (currentState.threads.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("💬", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Sé el primero en crear un hilo",
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
                            items(currentState.threads) { thread ->
                                ForumThreadCard(
                                    thread = thread,
                                    onClick = { onThreadClick(thread) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── DIÁLOGO CREAR HILO ────────────────────────────────────────────
        if (showCreateDialog) {
            CreateThreadDialog(
                isPosting = isPosting,
                onDismiss = { showCreateDialog = false },
                onCreate = { title, content, tag ->
                    viewModel.createThread(title, content, tag)
                    showCreateDialog = false
                }
            )
        }
    }
}

// Tarjeta de hilo del foro
@Composable
fun ForumThreadCard(thread: ForumThread, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Foto de perfil
                if (thread.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = thread.userPhoto,
                        contentDescription = thread.userName,
                        modifier = Modifier.size(36.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            thread.userName.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(thread.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(thread.timestamp)),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                // Tag
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = NeonBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        thread.tag,
                        color = NeonBlue,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                thread.title,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (thread.content.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    thread.content,
                    color = TextSecondary,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${thread.replyCount} respuestas",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }
        }
    }
}

// Diálogo para crear un nuevo hilo
@Composable
fun CreateThreadDialog(
    isPosting: Boolean,
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf(FORUM_TAGS.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Text("Nuevo hilo", color = TextPrimary, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Título
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Título", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonBlue
                    ),
                    singleLine = true
                )

                // Contenido
                OutlinedTextField(
                    value = content,
                    onValueChange = { content = it },
                    label = { Text("Descripción", color = TextSecondary) },
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonBlue,
                        unfocusedBorderColor = Color(0xFF333333),
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonBlue
                    ),
                    maxLines = 4
                )

                // Selector de etiqueta
                Text("Etiqueta", color = TextSecondary, fontSize = 13.sp)
                androidx.compose.foundation.lazy.LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(FORUM_TAGS) { tag ->
                        FilterChip(
                            selected = selectedTag == tag,
                            onClick = { selectedTag = tag },
                            label = { Text(tag, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonBlue,
                                selectedLabelColor = Color.White,
                                containerColor = Color(0xFF2A2A2A),
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onCreate(title, content, selectedTag) },
                enabled = title.isNotBlank() && !isPosting,
                colors = ButtonDefaults.buttonColors(containerColor = NeonBlue)
            ) {
                if (isPosting) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Publicar")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}