package org.weekendware.basil.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color

// ─────────────────────────────────────────────────────────────
// Raw Palette — these are the only place hex values live.
// Reference these from BasilColorScheme; never use them directly in UI.
// ─────────────────────────────────────────────────────────────

/**
 * The complete raw color palette for Basil.
 *
 * These are raw color values only — no semantic meaning is attached here.
 * Use [BasilColorScheme] tokens (via [LocalBasilColors]) in UI code instead.
 */
internal object BasilPalette {

    // Sage — brand family
    val Sage50  = Color(0xFFF4F6F3)
    val Sage100 = Color(0xFFE8EDE6)
    val Sage200 = Color(0xFFD1DBCE)
    /** The primary brand color. */
    val Sage300 = Color(0xFFB2BDAF)
    val Sage400 = Color(0xFF8FA08B)
    val Sage500 = Color(0xFF6B8267)
    val Sage600 = Color(0xFF546857)
    val Sage700 = Color(0xFF3D4E41)
    val Sage800 = Color(0xFF27342B)
    val Sage900 = Color(0xFF141A15)

    // Stone — warm neutral family
    val Stone50  = Color(0xFFF8F7F5)
    val Stone100 = Color(0xFFF0EEE9)
    val Stone200 = Color(0xFFE0DDD6)
    val Stone300 = Color(0xFFC9C5BC)
    val Stone400 = Color(0xFFA8A39A)
    val Stone500 = Color(0xFF7C776E)
    val Stone600 = Color(0xFF5C574F)
    val Stone700 = Color(0xFF3D3A34)
    val Stone800 = Color(0xFF2A2722)
    val Stone900 = Color(0xFF1A1816)

    // Glucose semantics — intentionally distinct from brand palette
    val GlucoseVeryHigh = Color(0xFFE05C1E)
    val GlucoseHigh     = Color(0xFFE8943A)
    val GlucoseInRange  = Color(0xFF5A9E6B)
    val GlucoseLow      = Color(0xFFD4A017)
    val GlucoseVeryLow  = Color(0xFFC42B2B)

    // Utility
    val White  = Color(0xFFFFFFFF)
    val Cream  = Color(0xFFFAF8F5)
    val Black  = Color(0xFF000000)
    val Scrim  = Color(0x52000000)

    // Error
    val Error          = Color(0xFFBA1A1A)
    val OnError        = Color(0xFFFFFFFF)
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)
}

// ─────────────────────────────────────────────────────────────
// Semantic Color Scheme
// ─────────────────────────────────────────────────────────────

/**
 * Basil's semantic color tokens. Every color decision in the UI should
 * reference one of these tokens rather than a raw [Color] value.
 *
 * Obtain the current scheme in a composable via [LocalBasilColors].current,
 * or via the [androidx.compose.material3.MaterialTheme.basilColors] extension.
 *
 * @property primary Main brand color — used for primary actions, FAB, active tabs.
 * @property onPrimary Content drawn on top of [primary].
 * @property primaryContainer Lightly-tinted container using the brand family.
 * @property onPrimaryContainer Content drawn on top of [primaryContainer].
 * @property secondary Supporting color for secondary actions and surfaces.
 * @property onSecondary Content drawn on top of [secondary].
 * @property secondaryContainer Lightly-tinted container using the secondary family.
 * @property onSecondaryContainer Content drawn on top of [secondaryContainer].
 * @property background The app's main background canvas.
 * @property onBackground Content drawn on top of [background].
 * @property surface Default card/sheet/dialog surface color.
 * @property onSurface Content drawn on top of [surface].
 * @property surfaceVariant Slightly differentiated surface for layered UI.
 * @property onSurfaceVariant Content drawn on top of [surfaceVariant].
 * @property glucoseVeryHigh Semantic color for very-high glucose readings (> ~250 mg/dL).
 * @property glucoseHigh Semantic color for high glucose readings (~180–250 mg/dL).
 * @property glucoseInRange Semantic color for in-range glucose readings (~70–180 mg/dL).
 * @property glucoseLow Semantic color for low glucose readings (~54–70 mg/dL).
 * @property glucoseVeryLow Semantic color for very-low/urgent glucose readings (< ~54 mg/dL).
 * @property error Color for error states.
 * @property onError Content drawn on top of [error].
 * @property errorContainer Lightly-tinted surface for error messaging.
 * @property onErrorContainer Content drawn on top of [errorContainer].
 * @property outline Color for borders and dividers.
 * @property outlineVariant Subtle variant for less prominent borders.
 * @property scrim Overlay scrim behind modals and sheets.
 * @property isDark Whether this scheme represents a dark-mode palette.
 */
data class BasilColorScheme(
    val primary: Color,
    val onPrimary: Color,
    val primaryContainer: Color,
    val onPrimaryContainer: Color,
    val secondary: Color,
    val onSecondary: Color,
    val secondaryContainer: Color,
    val onSecondaryContainer: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val surfaceVariant: Color,
    val onSurfaceVariant: Color,
    val glucoseVeryHigh: Color,
    val glucoseHigh: Color,
    val glucoseInRange: Color,
    val glucoseLow: Color,
    val glucoseVeryLow: Color,
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,
    val isDark: Boolean
)

// ─────────────────────────────────────────────────────────────
// Scheme instances
// ─────────────────────────────────────────────────────────────

/**
 * Returns the Basil light color scheme.
 *
 * Built on a warm cream background with sage-green brand tones.
 */
fun basilLightColorScheme(): BasilColorScheme = BasilColorScheme(
    primary             = BasilPalette.Sage600,
    onPrimary           = BasilPalette.White,
    primaryContainer    = BasilPalette.Sage100,
    onPrimaryContainer  = BasilPalette.Sage900,
    secondary           = BasilPalette.Stone500,
    onSecondary         = BasilPalette.White,
    secondaryContainer  = BasilPalette.Stone100,
    onSecondaryContainer = BasilPalette.Stone900,
    background          = BasilPalette.Cream,
    onBackground        = BasilPalette.Stone900,
    surface             = BasilPalette.White,
    onSurface           = BasilPalette.Stone900,
    surfaceVariant      = BasilPalette.Sage50,
    onSurfaceVariant    = BasilPalette.Stone600,
    glucoseVeryHigh     = BasilPalette.GlucoseVeryHigh,
    glucoseHigh         = BasilPalette.GlucoseHigh,
    glucoseInRange      = BasilPalette.GlucoseInRange,
    glucoseLow          = BasilPalette.GlucoseLow,
    glucoseVeryLow      = BasilPalette.GlucoseVeryLow,
    error               = BasilPalette.Error,
    onError             = BasilPalette.OnError,
    errorContainer      = BasilPalette.ErrorContainer,
    onErrorContainer    = BasilPalette.OnErrorContainer,
    outline             = BasilPalette.Stone300,
    outlineVariant      = BasilPalette.Stone200,
    scrim               = BasilPalette.Scrim,
    isDark              = false
)

/**
 * Returns the Basil dark color scheme.
 *
 * Deep stone-grey backgrounds with the brand sage lifted to a lighter shade.
 */
fun basilDarkColorScheme(): BasilColorScheme = BasilColorScheme(
    primary             = BasilPalette.Sage300,
    onPrimary           = BasilPalette.Sage900,
    primaryContainer    = BasilPalette.Sage700,
    onPrimaryContainer  = BasilPalette.Sage100,
    secondary           = BasilPalette.Stone300,
    onSecondary         = BasilPalette.Stone900,
    secondaryContainer  = BasilPalette.Stone700,
    onSecondaryContainer = BasilPalette.Stone100,
    background          = BasilPalette.Stone900,
    onBackground        = BasilPalette.Stone100,
    surface             = BasilPalette.Stone800,
    onSurface           = BasilPalette.Stone100,
    surfaceVariant      = BasilPalette.Stone700,
    onSurfaceVariant    = BasilPalette.Stone300,
    glucoseVeryHigh     = BasilPalette.GlucoseVeryHigh,
    glucoseHigh         = BasilPalette.GlucoseHigh,
    glucoseInRange      = BasilPalette.GlucoseInRange,
    glucoseLow          = BasilPalette.GlucoseLow,
    glucoseVeryLow      = BasilPalette.GlucoseVeryLow,
    error               = Color(0xFFFFB4AB),
    onError             = Color(0xFF690005),
    errorContainer      = Color(0xFF93000A),
    onErrorContainer    = Color(0xFFFFDAD6),
    outline             = BasilPalette.Stone500,
    outlineVariant      = BasilPalette.Stone700,
    scrim               = BasilPalette.Scrim,
    isDark              = true
)

// ─────────────────────────────────────────────────────────────
// Material3 mapping
// ─────────────────────────────────────────────────────────────

/**
 * Maps this [BasilColorScheme] to a Material3 [ColorScheme] so that
 * standard Material components (Button, TextField, etc.) automatically
 * reflect the Basil brand palette.
 */
fun BasilColorScheme.toMaterialColorScheme(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary             = primary,
            onPrimary           = onPrimary,
            primaryContainer    = primaryContainer,
            onPrimaryContainer  = onPrimaryContainer,
            secondary           = secondary,
            onSecondary         = onSecondary,
            secondaryContainer  = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background          = background,
            onBackground        = onBackground,
            surface             = surface,
            onSurface           = onSurface,
            surfaceVariant      = surfaceVariant,
            onSurfaceVariant    = onSurfaceVariant,
            error               = error,
            onError             = onError,
            errorContainer      = errorContainer,
            onErrorContainer    = onErrorContainer,
            outline             = outline,
            outlineVariant      = outlineVariant,
            scrim               = scrim
        )
    } else {
        lightColorScheme(
            primary             = primary,
            onPrimary           = onPrimary,
            primaryContainer    = primaryContainer,
            onPrimaryContainer  = onPrimaryContainer,
            secondary           = secondary,
            onSecondary         = onSecondary,
            secondaryContainer  = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background          = background,
            onBackground        = onBackground,
            surface             = surface,
            onSurface           = onSurface,
            surfaceVariant      = surfaceVariant,
            onSurfaceVariant    = onSurfaceVariant,
            error               = error,
            onError             = onError,
            errorContainer      = errorContainer,
            onErrorContainer    = onErrorContainer,
            outline             = outline,
            outlineVariant      = outlineVariant,
            scrim               = scrim
        )
    }

// ─────────────────────────────────────────────────────────────
// CompositionLocal
// ─────────────────────────────────────────────────────────────

/**
 * CompositionLocal that provides the current [BasilColorScheme] down the
 * composition tree. Defaults to the light scheme; [BasilTheme] overrides
 * this based on the system dark-mode setting.
 *
 * Prefer accessing colors via `MaterialTheme.basilColors` rather than
 * reading this local directly.
 */
val LocalBasilColors = compositionLocalOf<BasilColorScheme> { basilLightColorScheme() }
