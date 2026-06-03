package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SanctumColorScheme = lightColorScheme(
    primary = SanctumGold,
    secondary = SanctumSecondary,
    background = SanctumBackground,
    surface = SanctumSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = SanctumOnBackground,
    onSurface = SanctumOnSurface
)

private val CovenantColorScheme = darkColorScheme(
    primary = VoidNeonPurple,
    secondary = VoidSecondary,
    background = VoidBackground,
    surface = VoidSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = VoidOnBackground,
    onSurface = VoidOnSurface
)

private val IronColorScheme = darkColorScheme(
    primary = SlateBronze,
    secondary = SlateSecondary,
    background = SlateBackground,
    surface = SlateSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onBackground = SlateOnBackground,
    onSurface = SlateOnSurface
)

@Composable
fun RpgTheme(
    side: String = "NEUTRAL", // "SANCTUM", "COVENANT", "NEUTRAL"
    content: @Composable () -> Unit
) {
    val colorScheme = when (side) {
        "SANCTUM" -> SanctumColorScheme
        "COVENANT" -> CovenantColorScheme
        else -> IronColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

// Keep the old MyApplicationTheme for backwards compatibility if referenced by tests
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    RpgTheme(side = if (darkTheme) "NEUTRAL" else "SANCTUM", content = content)
}
