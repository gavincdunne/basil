package org.weekendware.basil.presentation.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Builds the Basil [Typography] scale for use in [BasilTheme].
 *
 * The scale is tuned for a health-tracking context where readability at
 * a glance is critical — larger line heights, slightly elevated body sizes,
 * and heavier weights for data values.
 *
 * ### When to use each style
 * | Style            | Use case                                             |
 * |------------------|------------------------------------------------------|
 * | `displayLarge`   | Hero glucose readings on the dashboard               |
 * | `displayMedium`  | Large numeric values (insulin dose, carbs)           |
 * | `headlineLarge`  | Top-level screen titles                              |
 * | `headlineMedium` | Section headers within a screen                     |
 * | `titleLarge`     | Card titles, sheet headers                          |
 * | `titleMedium`    | Sub-section labels                                  |
 * | `bodyLarge`      | Primary content paragraphs                          |
 * | `bodyMedium`     | Secondary content, list item text                   |
 * | `bodySmall`      | Captions, helper text                               |
 * | `labelLarge`     | Button text, prominent form labels                  |
 * | `labelMedium`    | Tab labels, chip text                               |
 * | `labelSmall`     | Timestamps, footnotes                               |
 *
 * To swap in a custom font family, set `fontFamily` on the styles below
 * and add the font resource to `composeApp/src/commonMain/composeResources/font/`.
 */
internal fun basilTypography(): Typography = Typography(

    displayLarge = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize   = 72.sp,
        lineHeight = 80.sp,
        letterSpacing = (-0.5).sp
    ),

    displayMedium = TextStyle(
        fontWeight = FontWeight.Light,
        fontSize   = 52.sp,
        lineHeight = 60.sp,
        letterSpacing = (-0.25).sp
    ),

    displaySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 0.sp
    ),

    headlineLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 30.sp,
        lineHeight = 38.sp,
        letterSpacing = 0.sp
    ),

    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize   = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp
    ),

    headlineSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),

    titleLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    titleMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),

    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 16.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.5.sp
    ),

    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 14.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.25.sp
    ),

    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize   = 12.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.4.sp
    ),

    labelLarge = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),

    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    ),

    labelSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize   = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
