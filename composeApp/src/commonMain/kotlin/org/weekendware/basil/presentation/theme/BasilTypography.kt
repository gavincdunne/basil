package org.weekendware.basil.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import basil.composeapp.generated.resources.Res
import basil.composeapp.generated.resources.dmsans_variable
import basil.composeapp.generated.resources.dmserifdisplay_regular
import org.jetbrains.compose.resources.Font

/** Sans-serif font family used for all UI text (labels, body, buttons). */
@Composable
fun dmSansFamily(): FontFamily = FontFamily(
    Font(Res.font.dmsans_variable, weight = FontWeight.Normal),
    Font(Res.font.dmsans_variable, weight = FontWeight.Medium),
    Font(Res.font.dmsans_variable, weight = FontWeight.SemiBold),
    Font(Res.font.dmsans_variable, weight = FontWeight.Bold)
)

/** Serif font family used exclusively for the "basil" wordmark. */
@Composable
fun dmSerifDisplayFamily(): FontFamily = FontFamily(
    Font(Res.font.dmserifdisplay_regular, weight = FontWeight.Normal)
)

/**
 * Builds the Basil [Typography] scale for use in [BasilTheme].
 *
 * Display styles use DM Serif Display; all other styles use DM Sans.
 * The scale is tuned for a health-tracking context — larger line heights and
 * heavier weights for glanceable data values.
 *
 * ### When to use each style
 * | Style            | Use case                                            |
 * |------------------|-----------------------------------------------------|
 * | `displaySmall`   | "basil" wordmark — DM Serif Display                 |
 * | `headlineSmall`  | Screen titles                                       |
 * | `titleLarge`     | Card titles, sheet headers                          |
 * | `bodyLarge`      | Primary content paragraphs                          |
 * | `bodyMedium`     | Secondary content, list item text                   |
 * | `labelLarge`     | Button text                                         |
 * | `labelSmall`     | Timestamps, ALL-CAPS tracking labels                |
 */
@Composable
internal fun basilTypography(): Typography {
    val sans  = dmSansFamily()
    val serif = dmSerifDisplayFamily()
    return buildTypography(sans = sans, serif = serif)
}

@Suppress("LongMethod")
private fun buildTypography(sans: FontFamily, serif: FontFamily): Typography = Typography(

    displayLarge = TextStyle(
        fontFamily    = serif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 72.sp,
        lineHeight    = 80.sp,
        letterSpacing = (-0.5).sp
    ),
    displayMedium = TextStyle(
        fontFamily    = serif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 52.sp,
        lineHeight    = 60.sp,
        letterSpacing = (-0.25).sp
    ),
    // Used for the "basil" wordmark on splash and auth screens.
    displaySmall = TextStyle(
        fontFamily    = serif,
        fontWeight    = FontWeight.Normal,
        fontSize      = 40.sp,
        lineHeight    = 48.sp,
        letterSpacing = (-0.5).sp
    ),

    headlineLarge = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 30.sp,
        lineHeight    = 38.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 24.sp,
        lineHeight    = 32.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Bold,
        fontSize      = 22.sp,
        lineHeight    = 28.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 18.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.15.sp
    ),
    titleSmall = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 14.sp,
        lineHeight    = 20.sp,
        letterSpacing = 0.1.sp
    ),

    bodyLarge = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 16.sp,
        lineHeight    = 26.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 14.sp,
        lineHeight    = 22.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Normal,
        fontSize      = 12.sp,
        lineHeight    = 18.sp,
        letterSpacing = 0.sp
    ),

    labelLarge = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.SemiBold,
        fontSize      = 16.sp,
        lineHeight    = 24.sp,
        letterSpacing = 0.sp
    ),
    labelMedium = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 12.sp,
        lineHeight    = 16.sp,
        letterSpacing = 0.8.sp
    ),
    labelSmall = TextStyle(
        fontFamily    = sans,
        fontWeight    = FontWeight.Medium,
        fontSize      = 11.sp,
        lineHeight    = 16.sp,
        letterSpacing = 2.0.sp
    )
)
