package com.example.recreationapp.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Define custom colors
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

// Vibrant color scheme
val LightBlue = Color(0xFF64B5F6)
val DeepPurple = Color(0xFF7C4DFF)
val Teal = Color(0xFF26A69A)
val Amber = Color(0xFFFFB300)
val Mint = Color(0xFF66BB6A)

// Dark mode complementary colors
val DarkBlue = Color(0xFF1E88E5)
val DarkPurple = Color(0xFF5E35B1)
val DarkTeal = Color(0xFF00897B)
val DarkAmber = Color(0xFFFFA000)
val DarkMint = Color(0xFF43A047)

// Theme color schemes
private val DarkColorScheme = darkColorScheme(
    primary = DeepPurple,
    onPrimary = Color.White,
    primaryContainer = DarkPurple,
    onPrimaryContainer = Color.White,
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = DarkTeal,
    onSecondaryContainer = Color.White,
    tertiary = Amber,
    onTertiary = Color.Black,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF242424),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF303030),
    error = Color(0xFFCF6679)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurple,
    onPrimary = Color.White,
    primaryContainer = Purple80,
    onPrimaryContainer = Color(0xFF3700B3),
    secondary = Teal,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFCEEAE7),
    onSecondaryContainer = Color(0xFF005B52),
    tertiary = Amber,
    onTertiary = Color.Black,
    background = Color.White,
    onBackground = Color.Black,
    surface = Color(0xFFF8F8F8),
    onSurface = Color.Black,
    surfaceVariant = Color(0xFFE7E0EC),
    error = Color(0xFFB00020)
)

// Global app theme state
object ThemeState {
    var isDarkTheme by mutableStateOf(false)
}

@Composable
fun RecreationAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme() || ThemeState.isDarkTheme,
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}