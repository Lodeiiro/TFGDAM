package com.marcolodeiro.gamelog.ui.screens.forum

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Send
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
import com.marcolodeiro.gamelog.data.model.ForumReply
import com.marcolodeiro.gamelog.data.model.ForumThread
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.ForumViewModel
import com.marcolodeiro.gamelog.viewmodel.ThreadState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ForumThreadScreen(
    thread: ForumThread,
    onBack: () -> Unit,
    viewModel: ForumViewModel = hiltViewModel()
) {
    val threadState by viewModel.threadState.collectAsState()
    val isPosting by viewModel.isPosting.collectAsState()
    var replyText by remember { mutableStateOf("") }

    // 🌟 CORRECCIÓN CRÍTICA: Forzamos la carga de datos de manera limpia y local.
    // Al usar el MainActivity tradicional con savedStateHandle, esta pantalla se monta
    // de cero correctamente y maneja el estado de carga (Loading) de forma interna.
    LaunchedEffect(thread.id) {
        viewModel.loadThread(thread)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── CABECERA ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
            }
            Text(
                text = thread.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = NeonBlue.copy(alpha = 0.15f),
                modifier = Modifier.padding(end = 8.dp)
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

        // ── CONTENIDO VIA STATE ───────────────────────────────────────────
        when (val currentState = threadState) {
            is ThreadState.Loading -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = NeonBlue)
                }
            }
            is ThreadState.Error -> {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(currentState.message, color = TextSecondary)
                }
            }
            is ThreadState.Success -> {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Post original del creador
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = NeonBlue.copy(alpha = 0.1f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
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
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(thread.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        Text(
                                            SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(thread.timestamp)),
                                            color = TextSecondary,
                                            fontSize = 11.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(thread.content, color = TextSecondary, fontSize = 14.sp, lineHeight = 20.sp)
                            }
                        }
                    }

                    // Contador de respuestas dinámico
                    item {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A2A2A))
                            Text(
                                "  ${currentState.replies.size} respuestas  ",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF2A2A2A))
                        }
                    }

                    // Lista de comentarios/respuestas de la comunidad
                    items(currentState.replies) { reply ->
                        ReplyCard(reply = reply)
                    }
                }
            }
        }

        // ── CAMPO DE RESPUESTA OPTIMIZADO ─────────────────────────────────
        HorizontalDivider(color = Color(0xFF2A2A2A))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = replyText,
                onValueChange = { replyText = it },
                placeholder = { Text("Escribe tu respuesta...", color = TextSecondary) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NeonBlue,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = NeonBlue
                ),
                maxLines = 3
            )
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = {
                    // 🌟 MEJORA: Eliminamos la dependencia estricta del estado en caliente.
                    // Usamos el objeto "thread" seguro inyectado en la vista para responder siempre bien.
                    if (replyText.isNotBlank() && !isPosting) {
                        viewModel.addReply(thread.id, replyText, thread)
                        replyText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .background(NeonBlue, CircleShape),
                enabled = replyText.isNotBlank() && !isPosting
            ) {
                Icon(Icons.Default.Send, contentDescription = "Enviar", tint = Color.White)
            }
        }
    }
}

// Tarjeta de respuesta con diseño scannable
@Composable
fun ReplyCard(reply: ForumReply) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (reply.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = reply.userPhoto,
                        contentDescription = reply.userName,
                        modifier = Modifier.size(30.dp).clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(NeonBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            reply.userName.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(reply.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Text(
                        SimpleDateFormat("dd/MM/yy HH:mm", Locale.getDefault()).format(Date(reply.timestamp)),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(reply.content, color = TextSecondary, fontSize = 13.sp, lineHeight = 20.sp)
        }
    }
}