package com.marcolodeiro.gamelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marcolodeiro.gamelog.data.model.ForumThread // Importamos el modelo del hilo
import com.marcolodeiro.gamelog.ui.Screen
import com.marcolodeiro.gamelog.ui.screens.auth.LoginScreen
import com.marcolodeiro.gamelog.ui.screens.chatbot.ChatbotScreen
import com.marcolodeiro.gamelog.ui.screens.detail.GameDetailScreen
import com.marcolodeiro.gamelog.ui.screens.explore.ExploreScreen
import com.marcolodeiro.gamelog.ui.screens.feed.FeedScreen
import com.marcolodeiro.gamelog.ui.screens.forum.ForumThreadScreen // Importamos la pantalla del hilo
import com.marcolodeiro.gamelog.ui.screens.home.HomeScreen
import com.marcolodeiro.gamelog.ui.screens.library.LibraryScreen
import com.marcolodeiro.gamelog.ui.screens.profile.ProfileScreen
import com.marcolodeiro.gamelog.ui.screens.profile.PublicProfileScreen
import com.marcolodeiro.gamelog.ui.screens.social.SearchUsersScreen
import com.marcolodeiro.gamelog.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameLogTheme {
                val navController = rememberNavController()
                val currentBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = currentBackStackEntry?.destination?.route

                Scaffold(
                    floatingActionButton = {
                        if (currentRoute != Screen.Login.route && currentRoute != Screen.Chatbot.route) {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate(Screen.Chatbot.route) {
                                        popUpTo(Screen.Home.route) { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                containerColor = AccentRed,
                                contentColor = TextPrimary
                            ) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = "Chatbot")
                            }
                        }
                    },
                    bottomBar = {
                        if (currentRoute != Screen.Login.route) {
                            BottomNavigationBar(navController, currentRoute)
                        }
                    }
                ) { paddingValues ->
                    AppNavHost(
                        navController = navController,
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController, currentRoute: String?) {
    val items = listOf(
        Screen.Home,
        Screen.Explore,
        Screen.Library,
        Screen.Feed,
        Screen.SearchUsers,
        Screen.Profile
    )

    NavigationBar(containerColor = SurfaceDark) {
        val itemColors = NavigationBarItemDefaults.colors(
            selectedIconColor = AccentRed,
            selectedTextColor = AccentRed,
            indicatorColor = SurfaceDark,
            unselectedIconColor = TextSecondary,
            unselectedTextColor = TextSecondary
        )

        items.forEach { screen ->
            NavigationBarItem(
                selected = currentRoute == screen.route,
                onClick = {
                    if (currentRoute != screen.route) {
                        navController.navigate(screen.route) {
                            if (screen == Screen.Home) {
                                popUpTo(Screen.Home.route) { inclusive = true }
                            } else {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = { screen.icon?.let { Icon(it, contentDescription = screen.title) } },
                label = { screen.title?.let { Text(it) } },
                colors = itemColors
            )
        }
    }
}

@Composable
fun AppNavHost(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route,
        modifier = modifier,
        enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToExplore = { navController.navigate(Screen.Explore.route) },
                onNavigateToLibrary = { navController.navigate(Screen.Library.route) },
                onNavigateToProfile = { navController.navigate(Screen.Profile.route) },
                onNavigateToChatbot = { navController.navigate(Screen.Chatbot.route) }
            )
        }

        composable(Screen.Explore.route) {
            ExploreScreen(
                onGameClick = { game ->
                    val gameJson = java.net.URLEncoder.encode(
                        com.google.gson.Gson().toJson(game),
                        "UTF-8"
                    ).replace("+", "%20")
                    navController.navigate(Screen.Detail.createRoute(gameJson))
                }
            )
        }

        composable(Screen.Library.route) {
            LibraryScreen()
        }

        composable(Screen.Feed.route) {
            FeedScreen(
                onUserClick = { uid ->
                    navController.navigate(Screen.PublicProfile.createRoute(uid))
                },
                onThreadClick = { thread ->
                    // Guardamos el objeto de manera segura en la cola de navegación
                    navController.currentBackStackEntry?.savedStateHandle?.set("selected_thread", thread)
                    navController.navigate(Screen.ForumThread.route)
                }
            )
        }

        composable(Screen.SearchUsers.route) {
            SearchUsersScreen(
                onUserClick = { user ->
                    navController.navigate(Screen.PublicProfile.createRoute(user.uid))
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onSignOut = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Detail.route) { backStackEntry ->
            val gameJson = backStackEntry.arguments?.getString("gameJson") ?: return@composable
            val game = com.google.gson.Gson().fromJson(
                gameJson,
                com.marcolodeiro.gamelog.data.model.Game::class.java
            )
            GameDetailScreen(
                game = game,
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Chatbot.route) {
            ChatbotScreen()
        }

        composable(Screen.PublicProfile.route) { backStackEntry ->
            val uid = backStackEntry.arguments?.getString("uid") ?: return@composable
            PublicProfileScreen(
                uid = uid,
                onBack = { navController.popBackStack() }
            )
        }

        // ── CORREGIDO: Recuperación limpia del hilo sin condicionar externamente el renderizado ──
        composable(Screen.ForumThread.route) {
            val thread = navController.previousBackStackEntry?.savedStateHandle?.get<ForumThread>("selected_thread")

            if (thread != null) {
                ForumThreadScreen(
                    thread = thread,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}