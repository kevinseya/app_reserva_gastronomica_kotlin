package com.jwmaila.appticketera.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = PrimaryYellow,
    onPrimary = NearBlack,
    primaryContainer = LightYellow,
    onPrimaryContainer = NearBlack,

    secondary = DarkAccent,
    onSecondary = White,
    secondaryContainer = LightYellow,
    onSecondaryContainer = NearBlack,

    tertiary = DarkGray,
    onTertiary = White,

    background = LightYellow,
    onBackground = NearBlack,

    surface = White,
    onSurface = NearBlack,
    surfaceVariant = LightGray,
    onSurfaceVariant = Gray,

    error = ErrorRed,
    onError = White
)

@Composable
fun TicketeraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
