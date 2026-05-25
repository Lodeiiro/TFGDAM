package com.marcolodeiro.gamelog.ui.screens.social

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
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
import com.marcolodeiro.gamelog.data.model.User
import com.marcolodeiro.gamelog.ui.theme.*
import com.marcolodeiro.gamelog.viewmodel.SearchUsersState
import com.marcolodeiro.gamelog.viewmodel.SearchUsersViewModel

// Pantalla para buscar y seguir a otros usuarios
@Composable
fun SearchUsersScreen(
    onUserClick: (User) -> Unit,
    viewModel: SearchUsersViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val followingMap by viewModel.followingMap.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // ── CABECERA con barra de búsqueda ────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(SurfaceDark)
                .padding(16.dp)
        ) {
            Text(
                text = "Buscar gamers",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onQueryChange(it) },
                placeholder = { Text("Buscar por nombre...", color = TextSecondary) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        TextButton(onClick = { viewModel.searchUsers() }) {
                            Text("Buscar", color = AccentRed)
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentRed,
                    unfocusedBorderColor = Color(0xFF333333),
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentRed
                ),
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { viewModel.searchUsers() }
                )
            )
        }

        // ── CONTENIDO ─────────────────────────────────────────────────────
        when (val currentState = state) {
            is SearchUsersState.Idle -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("👥", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Busca gamers por su nombre",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            is SearchUsersState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentRed)
                }
            }

            is SearchUsersState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(currentState.message, color = TextSecondary)
                }
            }

            is SearchUsersState.Success -> {
                if (currentState.users.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("🔍", fontSize = 48.sp)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron usuarios",
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
                        items(currentState.users) { user ->
                            UserCard(
                                user = user,
                                isFollowing = followingMap[user.uid] ?: false,
                                onFollowClick = { viewModel.toggleFollow(user.uid) },
                                onUserClick = { onUserClick(user) }
                            )
                        }
                    }
                }
            }
        }
    }
}

// Tarjeta de usuario con foto, nombre y botón de seguir
@Composable
fun UserCard(
    user: User,
    isFollowing: Boolean,
    onFollowClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Card(
        onClick = onUserClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Foto de perfil del usuario
            if (user.photoUrl.isNotBlank()) {
                AsyncImage(
                    model = user.photoUrl,
                    contentDescription = user.displayName,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = user.displayName.firstOrNull()?.toString() ?: "G",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Nombre y email
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = user.displayName,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email,
                    color = TextSecondary,
                    fontSize = 12.sp
                )
            }

            // Botón seguir / dejar de seguir
            Button(
                onClick = onFollowClick,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isFollowing) Color(0xFF2A2A2A) else AccentRed
                ),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (isFollowing) "Siguiendo" else "Seguir",
                    fontSize = 13.sp,
                    color = if (isFollowing) TextSecondary else Color.White
                )
            }
        }
    }
}