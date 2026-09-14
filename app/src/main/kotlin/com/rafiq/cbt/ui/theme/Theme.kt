package com.rafiq.cbt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val BgColor = Color(0xFF12181F)
val SurfaceColor = Color(0xFF1C242D)
val SurfaceColor2 = Color(0xFF232D38)
val TextColor = Color(0xFFE9E7DF)
val TextMuted = Color(0xFF95A1AC)
val LineColor = Color(0x14FFFFFF)
val GreenColor = Color(0xFF6E9B82)
val GreenDim = Color(0xFF4E6F5D)
val AmberColor = Color(0xFFD9A15B)
val AmberDim = Color(0xFF9C7743)
val RustColor = Color(0xFFB06554)
val RustDim = Color(0xFF7C4A3F)

private val DarkColors = darkColorScheme(
    background = BgColor,
    surface = SurfaceColor,
    primary = GreenColor,
    onPrimary = Color(0xFFEFF3F0),
    secondary = AmberColor,
    tertiary = RustColor,
    onBackground = TextColor,
    onSurface = TextColor,
    error = RustColor,
    outline = LineColor
)

@Composable
fun RafiqTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography(),
        content = content
    )
}
