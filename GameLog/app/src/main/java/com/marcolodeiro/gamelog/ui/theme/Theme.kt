package com.marcolodeiro.gamelog.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Paleta de colores estilo futurista neón ───────────────────────────────────
val DarkBackground  = Color(0xFF000000)   // Negro puro como Spotify
val SurfaceDark     = Color(0xFF0A0A0A)   // Superficie casi negra
val SurfaceCard     = Color(0xFF111111)   // Tarjetas ligeramente más claras
val SurfaceElevated = Color(0xFF1A1A1A)   // Elementos elevados

val NeonBlue        = Color(0xFF00A8FF)   // Azul neón — color principal
val NeonBlueLight   = Color(0xFF33BBFF)   // Versión más clara para hover
val NeonBlueDark    = Color(0xFF0080CC)   // Versión más oscura para pressed
val NeonBlueGlow    = Color(0x3300A8FF)   // Versión transparente para brillos

val TextPrimary     = Color(0xFFFFFFFF)   // Blanco puro
val TextSecondary   = Color(0xFFB3B3B3)   // Gris claro — igual que Spotify
val TextTertiary    = Color(0xFF535353)   // Gris oscuro para placeholders

// Colores de estado de juegos
val ColorPlaying    = Color(0xFF1DB954)   // Verde Spotify para "jugando"
val ColorCompleted  = Color(0xFF00A8FF)   // Azul neón
val ColorPlatinum   = Color(0xFFB3A0FF)   // Morado suave
val ColorPending    = Color(0xFFFF9800)   // Naranja
val ColorAbandoned  = Color(0xFFFF4444)   // Rojo
val ColorWishlist   = Color(0xFFFF69B4)   // Rosa

// Mantenemos AccentRed como alias de NeonBlue para no romper el código existente
val AccentRed       = NeonBlue



private val GameLogColorScheme = darkColorScheme(
    primary             = NeonBlue,
    onPrimary           = Color(0xFF000000),
    primaryContainer    = NeonBlueDark,
    onPrimaryContainer  = Color(0xFFFFFFFF),
    secondary           = NeonBlueLight,
    onSecondary         = Color(0xFF000000),
    background          = DarkBackground,
    surface             = SurfaceDark,
    surfaceVariant      = SurfaceCard,
    onBackground        = TextPrimary,
    onSurface           = TextPrimary,
    onSurfaceVariant    = TextSecondary,
    outline             = Color(0xFF282828),
    error               = Color(0xFFFF4444),
)

@Composable
fun GameLogTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = GameLogColorScheme,
        typography  = Typography(),
        content     = content
    )
}