package com.marcolodeiro.gamelog

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LibraryBooks
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.marcolodeiro.gamelog.ui.screens.auth.LoginScreen
import com.marcolodeiro.gamelog.ui.screens.chatbot.ChatbotScreen
import com.marcolodeiro.gamelog.ui.screens.detail.GameDetailScreen
import com.marcolodeiro.gamelog.ui.screens.explore.ExploreScreen
import com.marcolodeiro.gamelog.ui.screens.feed.FeedScreen
import com.marcolodeiro.gamelog.ui.screens.home.HomeScreen
import com.marcolodeiro.gamelog.ui.screens.library.LibraryScreen
import com.marcolodeiro.gamelog.ui.screens.profile.ProfileScreen
import com.marcolodeiro.gamelog.ui.screens.social.SearchUsersScreen
import com.marcolodeiro.gamelog.ui.theme.AccentRed
import com.marcolodeiro.gamelog.ui.theme.GameLogTheme
import com.marcolodeiro.gamelog.ui.theme.SurfaceDark
import com.marcolodeiro.gamelog.ui.theme.TextPrimary
import com.marcolodeiro.gamelog.ui.theme.TextSecondary
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GameLogTheme {
                val navController = rememberNavController()
                val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route

                Scaffold(
                    floatingActionButton = {
                        if (currentRoute != "login") {
                            FloatingActionButton(
                                onClick = {
                                    navController.navigate("chatbot") {
                                        popUpTo("home") { saveState = true }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                containerColor = AccentRed,
                                contentColor = TextPrimary
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = "Chatbot"
                                )
                            }
                        }
                    },
                    bottomBar = {
                        if (currentRoute != "login") {
                            NavigationBar(containerColor = SurfaceDark) {
                                NavigationBarItem(
                                    selected = currentRoute == "home",
                                    onClick = {
                                        navController.navigate("home") {
                                            popUpTo("home") { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = "Inicio") },
                                    label = { Text("Inicio") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "explore",
                                    onClick = { navController.navigate("explore") },
                                    icon = { Icon(Icons.Default.Search, contentDescription = "Explorar") },
                                    label = { Text("Explorar") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "library",
                                    onClick = { navController.navigate("library") },
                                    icon = { Icon(Icons.Default.LibraryBooks, contentDescription = "Biblioteca") },
                                    label = { Text("Biblioteca") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "feed",
                                    onClick = { navController.navigate("feed") },
                                    icon = { Icon(Icons.Default.DynamicFeed, contentDescription = "Feed") },
                                    label = { Text("Feed") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "search_users",
                                    onClick = { navController.navigate("search_users") },
                                    icon = { Icon(Icons.Default.People, contentDescription = "Gamers") },
                                    label = { Text("Gamers") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                                NavigationBarItem(
                                    selected = currentRoute == "profile",
                                    onClick = { navController.navigate("profile") },
                                    icon = { Icon(Icons.Default.Person, contentDescription = "Perfil") },
                                    label = { Text("Perfil") },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = AccentRed,
                                        selectedTextColor = AccentRed,
                                        indicatorColor = SurfaceDark,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary
                                    )
                                )
                            }
                        }
                    }
                ) { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "login",
                        modifier = Modifier.padding(paddingValues),
                        enterTransition = {
                            slideInHorizontally(initialOffsetX = { it }) + fadeIn()
                        },
                        exitTransition = {
                            slideOutHorizontally(targetOffsetX = { -it }) + fadeOut()
                        },
                        popEnterTransition = {
                            slideInHorizontally(initialOffsetX = { -it }) + fadeIn()
                        },
                        popExitTransition = {
                            slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                        }
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home") {
                            HomeScreen(
                                onNavigateToExplore = { navController.navigate("explore") },
                                onNavigateToLibrary = { navController.navigate("library") },
                                onNavigateToProfile = { navController.navigate("profile") }
                            )
                        }
                        composable("explore") {
                            ExploreScreen(
                                onGameClick = { game ->
                                    val gameJson = java.net.URLEncoder.encode(
                                        com.google.gson.Gson().toJson(game),
                                        "UTF-8"
                                    ).replace("+", "%20")
                                    navController.navigate("detail/$gameJson")
                                }
                            )
                        }
                        composable("library") {
                            LibraryScreen()
                        }
                        composable("profile") {
                            ProfileScreen(
                                onSignOut = {
                                    navController.navigate("login") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("search_users") {
                            SearchUsersScreen(
                                onUserClick = { user -> }
                            )
                        }
                        composable("detail/{gameJson}") { backStackEntry ->
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
                        composable("feed") {
                            FeedScreen()
                        }
                        composable("chatbot") {
                            ChatbotScreen()
                        }
                    }
                }
            }
        }
    }
}
