package com.example.counter.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Expressive corner language: generously rounded, so the shapes stay soft at every size. */
private val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(22.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/** Display styles are pulled tighter and heavier, the way expressive type wants to look. */
private val ExpressiveTypography: Typography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-2).sp,
        ),
        displayMedium = base.displayMedium.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1.5).sp,
        ),
        displaySmall = base.displaySmall.copy(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Medium,
            letterSpacing = (-1).sp,
        ),
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
    )
}

/**
 * Monet first: on Android 12+ the whole palette comes from the wallpaper via the system's
 * dynamic colour engine. Older devices fall back to the static expressive baseline scheme so the
 * look stays in the family.
 */
@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun rememberColorScheme(darkTheme: Boolean, dynamicColor: Boolean): ColorScheme {
    val context = LocalContext.current
    return remember(darkTheme, dynamicColor) {
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)

            darkTheme -> darkColorScheme()
            else -> expressiveLightColorScheme()
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CounterTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = rememberColorScheme(darkTheme = darkTheme, dynamicColor = dynamicColor),
        motionScheme = MotionScheme.expressive(),
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content,
    )
}