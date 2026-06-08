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
import com.marcolodeiro.gamelog.data.model.Review
import com.marcolodeiro.gamelog.data.model.NewsArticle
import com.marcolodeiro.gamelog.viewmodel.NewsState
import com.marcolodeiro.gamelog.viewmodel.NewsViewModel

@Composable
fun FeedScreen(
    onUserClick: (String) -> Unit, // 👈 Recibido de MainActivity
    viewModel: FeedViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    val reviews by viewModel.reviews.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Actividad", "Opiniones", "Noticias")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
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

        // 👈 CORREGIDO: Ahora pasamos onUserClick a las pestañas correspondientes
        when (selectedTab) {
            0 -> ActivityTabContent(state, viewModel, onlyReviews = false, onUserClick = onUserClick)
            1 -> ReviewsTabContent(reviews, onUserClick = onUserClick)
            2 -> NewsTabContent()
        }
    }
}

@Composable
fun ActivityTabContent(
    state: FeedState,
    viewModel: FeedViewModel,
    onlyReviews: Boolean,
    onUserClick: (String) -> Unit // 👈 Pasado por parámetro
) {
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
            val itemsToShow = if (onlyReviews) {
                currentState.items.filter { it.status == "COMPLETED" || it.status == "PLATINUM" }
            } else {
                currentState.items
            }

            if (itemsToShow.isEmpty()) {
                val emptyText = if (onlyReviews) "Aún no hay opiniones o análisis de juegos publicados." else "Sigue a otros gamers para ver su actividad."
                EmptyStateView(icon = if (onlyReviews) "📝" else "👥", text = emptyText)
            } else {
                // 👈 CORREGIDO: Rellenado el LazyColumn para pintar los FeedItemCard reales
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemsToShow) { item ->
                        FeedItemCard(
                            item = item,
                            onClick = { onUserClick(item.userId) } // 👈 Al pulsar, envía el userId
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NewsTabContent(
    viewModel: NewsViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()

    when (val currentState = state) {
        is NewsState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
        }
        is NewsState.Error -> {
            EmptyStateView(icon = "📰", text = "No se pudieron cargar las noticias")
        }
        is NewsState.Success -> {
            if (currentState.articles.isEmpty()) {
                EmptyStateView(icon = "📰", text = "No hay noticias disponibles")
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(currentState.articles) { article ->
                        NewsCard(article = article)
                    }
                }
            }
        }
    }
}

@Composable
fun NewsCard(article: NewsArticle) {
    val context = androidx.compose.ui.platform.LocalContext.current

    Card(
        onClick = {
            // Abre la noticia en el navegador
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(article.url)
            )
            context.startActivity(intent)
        },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            // Imagen de la noticia
            if (article.urlToImage.isNotBlank()) {
                AsyncImage(
                    model = article.urlToImage,
                    contentDescription = article.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.padding(14.dp)) {
                // Fuente y fecha
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonBlue.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = article.source.name,
                            color = NeonBlue,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Text(
                        text = article.publishedAt.take(10),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Título
                Text(
                    text = article.title,
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Descripción
                if (article.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = article.description,
                        color = TextSecondary,
                        fontSize = 13.sp,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Botón leer más
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Leer más →",
                        color = NeonBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
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
fun FeedItemCard(
    item: FeedItem,
    onClick: () -> Unit // 👈 Parámetro de click añadido
) {
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
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick // 👈 CORREGIDO: Hacemos la tarjeta clickable
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

// Pestaña de opiniones con reseñas reales de Firestore
@Composable
fun ReviewsTabContent(
    reviews: List<Review>,
    onUserClick: (String) -> Unit // 👈 Añadido parámetro
) {
    if (reviews.isEmpty()) {
        EmptyStateView(
            icon = "📝",
            text = "Sigue a otros gamers para ver sus reseñas aquí"
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(reviews) { review ->
                ReviewFeedCard(
                    review = review,
                    onClick = { onUserClick(review.userId) } // 👈 Captura el click
                )
            }
        }
    }
}

// Tarjeta de reseña en el feed
@Composable
fun ReviewFeedCard(
    review: Review,
    onClick: () -> Unit // 👈 Parámetro de click añadido
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick // 👈 CORREGIDO: Añadido click para ir al perfil
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Cabecera con usuario
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (review.userPhoto.isNotBlank()) {
                    AsyncImage(
                        model = review.userPhoto,
                        contentDescription = review.userName,
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
                            review.userName.firstOrNull()?.toString() ?: "G",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(review.userName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(
                        SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(review.timestamp)),
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }
                // Puntuación
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonBlue.copy(alpha = 0.15f)
                ) {
                    Text(
                        "⭐ ${review.rating.toInt()}/10",
                        color = NeonBlue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(modifier = Modifier.height(10.dp))

            // Nombre del juego
            Text(
                review.gameName,
                color = NeonBlue,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            // Texto de la reseña
            Text(
                review.text,
                color = TextSecondary,
                fontSize = 13.sp,
                lineHeight = 20.sp
            )
        }
    }
}