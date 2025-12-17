package com.example.sleeptandard_mvp_demo.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.sleeptandard_mvp_demo.ui.theme.LightBackground

private val DarkColorScheme = darkColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = Pink80,
    surface = LightSurface,
    onSurface = BlackFont,
)

private val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    secondary = LightSecondary,
    tertiary = Pink40,
    surface = LightSurface,
    onSurface = BlackFont,
    surfaceDim = DeactivateSurface


    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun Sleeptandard_MVP_DemoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // 안드로이드12 부터 자동으로 사용자 환경에 자연스러운 색으로 맞춰주는 DynamicColor시스템. 일단 false로 두겠음.
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}