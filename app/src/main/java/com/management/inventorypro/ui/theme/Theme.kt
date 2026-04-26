package com.management.inventorypro.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)
private val MidnightColorScheme = darkColorScheme(
    primary = ElectricBlue,
    secondary = SoftBlue,
    tertiary = Color.Cyan,
    background = DeepNavy,
    surface = SpaceBlue,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onBackground = OffWhite,
    onSurface = OffWhite,
    surfaceVariant = Color(0xFF1C222D))

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

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

private val NeonBlueColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricViolet,
    background = DeepMidnight,
    surface = SurfaceBlue,

    onPrimary = Color.Black,        // Dark text on bright cyan buttons
    onBackground = NeonText,        // Soft white-blue text on dark background
    onSurface = NeonText,

    outline = BorderBlue,           // Used for OutlinedTextFields
    surfaceVariant = Color(0xFF1C2541) // For search bars or header backgrounds
)

@Composable
fun InventoryProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }
    val colorScheme = MidnightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )

//    MaterialTheme(
//        colorScheme = colorScheme,
//        typography = Typography,
//        content = content
//    )
}