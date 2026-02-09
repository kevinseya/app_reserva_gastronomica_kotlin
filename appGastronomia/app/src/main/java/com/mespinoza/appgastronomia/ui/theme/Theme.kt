package com.mespinoza.appgastronomia.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryYellow,
    onPrimary = NearBlack,
    primaryContainer = DarkAccent,
    onPrimaryContainer = NearBlack,

    secondary = DarkAccent,
    onSecondary = White,
    secondaryContainer = PrimaryYellow,
    onSecondaryContainer = NearBlack,

    tertiary = DarkGray,
    onTertiary = White,

    background = NearBlack,
    onBackground = White,

    surface = DarkGray,
    onSurface = White,
    surfaceVariant = Gray,
    onSurfaceVariant = White,

    error = ErrorRed,
    onError = White
)

@Composable
fun GastronomiaTheme(
    content: @Composable () -> Unit
) {
    val gradientBrush = Brush.verticalGradient(
        colors = listOf(NearBlack, PrimaryYellow)
    )

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(brush = gradientBrush)
        ) {
            content()
        }
    }
}
