package com.marcolodeiro.gamelog.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    // Pantalla de Auth
    object Login : Screen("login")

    // Las 6 pantallas de tu barra inferior (Se quedan todas)
    object Home : Screen("home", "Inicio", Icons.Default.Home)
    object Explore : Screen("explore", "Explorar", Icons.Default.Search)
    object Library : Screen("library", "Biblioteca", Icons.Default.LibraryBooks)
    object Feed : Screen("feed", "Feed", Icons.Default.DynamicFeed)
    object SearchUsers : Screen("search_users", "Gamers", Icons.Default.People)
    object Profile : Screen("profile", "Perfil", Icons.Default.Person)

    // Pantallas secundarias
    object Chatbot : Screen("chatbot")
    object Detail : Screen("detail/{gameJson}") {
        fun createRoute(gameJson: String) = "detail/$gameJson"
    }

    object PublicProfile : Screen("public_profile/{uid}") {
        fun createRoute(uid: String) = "public_profile/$uid"
    }

    object Forum : Screen("forum")
    object ForumThread : Screen("forum_thread/{threadJson}") {
        fun createRoute(threadJson: String) = "forum_thread/$threadJson"
    }
}