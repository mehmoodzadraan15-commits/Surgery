package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = SurgicalTealPrimaryDark,
    onPrimary = SurgicalTealOnPrimaryDark,
    primaryContainer = SurgicalTealContainerDark,
    secondary = SurgicalNavySecondaryDark,
    onSecondary = SurgicalNavyOnSecondaryDark,
    secondaryContainer = SurgicalNavyContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    surfaceVariant = SurfaceVariantDark,
    onSurface = OnSurfaceDark,
    onSurfaceVariant = OnSurfaceVariantDark
)

private val LightColorScheme = lightColorScheme(
    primary = SurgicalTealPrimary,
    onPrimary = SurgicalTealOnPrimary,
    primaryContainer = SurgicalTealContainer,
    secondary = SurgicalNavySecondary,
    onSecondary = SurgicalNavyOnSecondary,
    secondaryContainer = SurgicalNavyContainer,
    background = BackgroundLight,
    surface = SurfaceLight,
    surfaceVariant = SurfaceVariantLight,
    onSurface = OnSurfaceLight,
    onSurfaceVariant = OnSurfaceVariantLight
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our crisp tailored medical palette by default
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
