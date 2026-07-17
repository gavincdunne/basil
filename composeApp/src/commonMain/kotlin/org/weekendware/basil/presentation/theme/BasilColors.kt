package org.weekendware.basil.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Color
import kotlin.math.abs

// ─────────────────────────────────────────────────────────────
// Tonal Palette Generator
//
// Derives a 10-stop tonal palette from a single seed hue + chroma.
// Change SageHue or SageChroma and all palette entries update automatically.
// HSL → RGB conversion is pure Kotlin — no android.graphics dependency.
// ─────────────────────────────────────────────────────────────

/** A 10-stop tonal palette derived from a single seed hue and chroma. */
data class TonalPalette(
    val shade50:  Color,
    val shade100: Color,
    val shade200: Color,
    val shade300: Color,
    val shade400: Color,
    val shade500: Color,
    val shade600: Color,
    val shade700: Color,
    val shade800: Color,
    val shade900: Color,
)

/**
 * Generates a [TonalPalette] from a seed [hue] (0–360°) and [chroma] (0–1 saturation).
 *
 * Lightness follows Material Design's tonal scale. Saturation tapers slightly
 * at the extremes so very light and very dark shades stay clean.
 */
fun basilTonalPalette(hue: Float, chroma: Float): TonalPalette {
    fun shade(lightness: Float, chromaScale: Float = 1f) =
        hslToColor(hue, chroma * chromaScale, lightness)
    return TonalPalette(
        shade50  = shade(0.96f, 0.55f),
        shade100 = shade(0.92f, 0.70f),
        shade200 = shade(0.84f, 0.85f),
        shade300 = shade(0.75f, 0.95f),
        shade400 = shade(0.63f, 1.00f),
        shade500 = shade(0.50f, 1.00f),
        shade600 = shade(0.40f, 1.00f),
        shade700 = shade(0.30f, 0.95f),
        shade800 = shade(0.20f, 0.85f),
        shade900 = shade(0.12f, 0.70f),
    )
}

/** Pure Kotlin HSL → Compose [Color]. h: 0–360, s: 0–1, l: 0–1. */
private fun hslToColor(h: Float, s: Float, l: Float): Color {
    val c = (1f - abs(2f * l - 1f)) * s
    val x = c * (1f - abs((h / 60f) % 2f - 1f))
    val m = l - c / 2f
    val (r1, g1, b1) = when {
        h < 60f  -> Triple(c, x, 0f)
        h < 120f -> Triple(x, c, 0f)
        h < 180f -> Triple(0f, c, x)
        h < 240f -> Triple(0f, x, c)
        h < 300f -> Triple(x, 0f, c)
        else     -> Triple(c, 0f, x)
    }
    return Color(r1 + m, g1 + m, b1 + m)
}

// ─────────────────────────────────────────────────────────────
// Seed values — change these to retheme the entire app.
// ─────────────────────────────────────────────────────────────

private const val SageHue    = 130f
private const val SageChroma = 0.22f   // moderate green saturation

private const val StoneHue    = 35f
private const val StoneChroma = 0.06f  // warm, barely saturated

private const val AmberHue    = 40f
private const val AmberChroma = 0.50f  // warm amber for evening accents

internal val BasilSagePalette  = basilTonalPalette(SageHue, SageChroma)
internal val BasilStonePalette = basilTonalPalette(StoneHue, StoneChroma)
internal val BasilAmberPalette = basilTonalPalette(AmberHue, AmberChroma)

// ─────────────────────────────────────────────────────────────
// Raw Palette — computed from seeds above.
// Named aliases kept for readability; generated values are very
// close to the original hand-picked hex values they replace.
// Never reference these directly in UI — use BasilColorScheme tokens.
// ─────────────────────────────────────────────────────────────

internal object BasilPalette {

    // Sage — brand family (derived)
    val Sage50  get() = BasilSagePalette.shade50
    val Sage100 get() = BasilSagePalette.shade100
    val Sage200 get() = BasilSagePalette.shade200
    val Sage300 get() = BasilSagePalette.shade300
    val Sage400 get() = BasilSagePalette.shade400
    val Sage500 get() = BasilSagePalette.shade500
    /** The primary brand color. */
    val Sage600 get() = BasilSagePalette.shade600
    val Sage700 get() = BasilSagePalette.shade700
    val Sage800 get() = BasilSagePalette.shade800
    val Sage900 get() = BasilSagePalette.shade900

    // Stone — warm neutral family (derived)
    val Stone50  get() = BasilStonePalette.shade50
    val Stone100 get() = BasilStonePalette.shade100
    val Stone200 get() = BasilStonePalette.shade200
    val Stone300 get() = BasilStonePalette.shade300
    val Stone400 get() = BasilStonePalette.shade400
    val Stone500 get() = BasilStonePalette.shade500
    val Stone600 get() = BasilStonePalette.shade600
    val Stone700 get() = BasilStonePalette.shade700
    val Stone800 get() = BasilStonePalette.shade800
    val Stone900 get() = BasilStonePalette.shade900

    // Utility
    val White = Color(0xFFFFFFFF)
    val Cream = Color(0xFFFAF8F5)
    val Black = Color(0xFF000000)
    val Scrim = Color(0x52000000)

    // Error (intentionally not derived — universal semantic colour)
    val Error             = Color(0xFFBA1A1A)
    val OnError           = Color(0xFFFFFFFF)
    val ErrorContainer    = Color(0xFFFFDAD6)
    val OnErrorContainer  = Color(0xFF410002)
}

// ─────────────────────────────────────────────────────────────
// Semantic Color Scheme
// ─────────────────────────────────────────────────────────────

/**
 * Basil's semantic color tokens. Every color decision in the UI should
 * reference one of these tokens rather than a raw [Color] value.
 *
 * Obtain the current scheme in a composable via [LocalBasilColors].current.
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
 * @property error Color for error states.
 * @property onError Content drawn on top of [error].
 * @property errorContainer Lightly-tinted surface for error messaging.
 * @property onErrorContainer Content drawn on top of [errorContainer].
 * @property outline Color for borders and dividers.
 * @property outlineVariant Subtle variant for less prominent borders.
 * @property warmAccent Warm amber accent — used in evening scheme for borders and surface-variant tint.
 * @property onWarmAccent Content drawn on top of [warmAccent].
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
    val error: Color,
    val onError: Color,
    val errorContainer: Color,
    val onErrorContainer: Color,
    val outline: Color,
    val outlineVariant: Color,
    val scrim: Color,
    val isDark: Boolean,
    // Evening-specific warm accent; defaults to outline so other schemes are unaffected.
    val warmAccent: Color = outline,
    val onWarmAccent: Color = onSurface,
)

// ─────────────────────────────────────────────────────────────
// Day/night scheme constructors
// ─────────────────────────────────────────────────────────────

/**
 * Morning scheme (~5am–10am). Lighter sage, slightly cool.
 * The plant is waking up — pale, fresh, hopeful.
 */
fun basilMorningColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilPalette.Sage500,
    onPrimary            = BasilPalette.White,
    primaryContainer     = BasilPalette.Sage100,
    onPrimaryContainer   = BasilPalette.Sage900,
    secondary            = BasilPalette.Stone400,
    onSecondary          = BasilPalette.White,
    secondaryContainer   = BasilPalette.Stone100,
    onSecondaryContainer = BasilPalette.Stone900,
    background           = hslToColor(130f, 0.06f, 0.96f), // pale sage-tinted cream
    onBackground         = BasilPalette.Stone900,
    surface              = BasilPalette.White,
    onSurface            = BasilPalette.Stone900,
    surfaceVariant       = BasilPalette.Sage50,
    onSurfaceVariant     = BasilPalette.Stone500,
    error                = BasilPalette.Error,
    onError              = BasilPalette.OnError,
    errorContainer       = BasilPalette.ErrorContainer,
    onErrorContainer     = BasilPalette.OnErrorContainer,
    outline              = BasilPalette.Stone200,
    outlineVariant       = BasilPalette.Stone100,
    scrim                = BasilPalette.Scrim,
    isDark               = false,
)

/**
 * Day scheme (10am–6pm). The reference state — clean cream, full sage.
 */
fun basilDayColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilPalette.Sage600,
    onPrimary            = BasilPalette.White,
    primaryContainer     = BasilPalette.Sage100,
    onPrimaryContainer   = BasilPalette.Sage900,
    secondary            = BasilPalette.Stone500,
    onSecondary          = BasilPalette.White,
    secondaryContainer   = BasilPalette.Stone100,
    onSecondaryContainer = BasilPalette.Stone900,
    background           = BasilPalette.Cream,
    onBackground         = BasilPalette.Stone900,
    surface              = BasilPalette.White,
    onSurface            = BasilPalette.Stone900,
    surfaceVariant       = BasilPalette.Sage50,
    onSurfaceVariant     = BasilPalette.Stone600,
    error                = BasilPalette.Error,
    onError              = BasilPalette.OnError,
    errorContainer       = BasilPalette.ErrorContainer,
    onErrorContainer     = BasilPalette.OnErrorContainer,
    outline              = BasilPalette.Stone300,
    outlineVariant       = BasilPalette.Stone200,
    scrim                = BasilPalette.Scrim,
    isDark               = false,
)

/**
 * Evening scheme (6pm–9pm). Cream pulls warm; amber accent on borders.
 * Golden hour on the leaves.
 */
fun basilEveningColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilPalette.Sage600,
    onPrimary            = BasilPalette.White,
    primaryContainer     = BasilPalette.Sage100,
    onPrimaryContainer   = BasilPalette.Sage900,
    secondary            = BasilPalette.Stone500,
    onSecondary          = BasilPalette.White,
    secondaryContainer   = BasilPalette.Stone100,
    onSecondaryContainer = BasilPalette.Stone900,
    background           = hslToColor(35f, 0.20f, 0.96f), // warm amber-cream
    onBackground         = BasilPalette.Stone900,
    surface              = hslToColor(35f, 0.10f, 0.99f), // barely-warm white
    onSurface            = BasilPalette.Stone900,
    surfaceVariant       = hslToColor(35f, 0.15f, 0.93f), // warm tint on surface variant
    onSurfaceVariant     = BasilPalette.Stone600,
    error                = BasilPalette.Error,
    onError              = BasilPalette.OnError,
    errorContainer       = BasilPalette.ErrorContainer,
    onErrorContainer     = BasilPalette.OnErrorContainer,
    outline              = BasilPalette.Stone300,
    outlineVariant       = BasilPalette.Stone200,
    scrim                = BasilPalette.Scrim,
    isDark               = false,
    warmAccent           = BasilAmberPalette.shade400,   // muted gold for borders/accents
    onWarmAccent         = BasilPalette.Stone900,
)

/**
 * Night scheme (9pm–5am). Deep stone backgrounds; sage becomes luminous.
 * Intimate and still — someone is still there.
 */
fun basilNightColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilPalette.Sage300,
    onPrimary            = BasilPalette.Sage900,
    primaryContainer     = BasilPalette.Sage700,
    onPrimaryContainer   = BasilPalette.Sage100,
    secondary            = BasilPalette.Stone300,
    onSecondary          = BasilPalette.Stone900,
    secondaryContainer   = BasilPalette.Stone700,
    onSecondaryContainer = BasilPalette.Stone100,
    background           = BasilPalette.Stone900,
    onBackground         = BasilPalette.Stone100,
    surface              = BasilPalette.Stone800,
    onSurface            = BasilPalette.Stone100,
    surfaceVariant       = BasilPalette.Stone700,
    onSurfaceVariant     = BasilPalette.Stone300,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = BasilPalette.Stone500,
    outlineVariant       = BasilPalette.Stone700,
    scrim                = BasilPalette.Scrim,
    isDark               = true,
)

// ─────────────────────────────────────────────────────────────
// Time-of-day scheme selector
//
// Call basilSchemeForHour(LocalTime.now().hour) in BasilTheme.
// Transitions between schemes should be animated via animateColorAsState
// at the BasilTheme composable level — not implemented here.
// ─────────────────────────────────────────────────────────────

/**
 * Returns the appropriate [BasilColorScheme] for the given [hour] (0–23).
 * Wire this in BasilTheme and animate transitions with animateColorAsState.
 */
fun basilSchemeForHour(hour: Int): BasilColorScheme = when (hour) {
    in 5..9   -> basilMorningColorScheme()
    in 10..17 -> basilDayColorScheme()
    in 18..20 -> basilEveningColorScheme()
    else      -> basilNightColorScheme()
}

// ─────────────────────────────────────────────────────────────
// Backward-compatible aliases
// ─────────────────────────────────────────────────────────────

/** Alias for [basilDayColorScheme]. Prefer the named day/night constructors. */
fun basilLightColorScheme(): BasilColorScheme = basilDayColorScheme()

/** Alias for [basilNightColorScheme]. Prefer the named day/night constructors. */
fun basilDarkColorScheme(): BasilColorScheme = basilNightColorScheme()

// ─────────────────────────────────────────────────────────────
// Material3 mapping
// ─────────────────────────────────────────────────────────────

/**
 * Maps this [BasilColorScheme] to a Material3 [ColorScheme] so that
 * standard Material components automatically reflect the Basil palette.
 */
fun BasilColorScheme.toMaterialColorScheme(): ColorScheme =
    if (isDark) {
        darkColorScheme(
            primary              = primary,
            onPrimary            = onPrimary,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = onSecondary,
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background           = background,
            onBackground         = onBackground,
            surface              = surface,
            onSurface            = onSurface,
            surfaceVariant       = surfaceVariant,
            onSurfaceVariant     = onSurfaceVariant,
            error                = error,
            onError              = onError,
            errorContainer       = errorContainer,
            onErrorContainer     = onErrorContainer,
            outline              = outline,
            outlineVariant       = outlineVariant,
            scrim                = scrim,
        )
    } else {
        lightColorScheme(
            primary              = primary,
            onPrimary            = onPrimary,
            primaryContainer     = primaryContainer,
            onPrimaryContainer   = onPrimaryContainer,
            secondary            = secondary,
            onSecondary          = onSecondary,
            secondaryContainer   = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            background           = background,
            onBackground         = onBackground,
            surface              = surface,
            onSurface            = onSurface,
            surfaceVariant       = surfaceVariant,
            onSurfaceVariant     = onSurfaceVariant,
            error                = error,
            onError              = onError,
            errorContainer       = errorContainer,
            onErrorContainer     = onErrorContainer,
            outline              = outline,
            outlineVariant       = outlineVariant,
            scrim                = scrim,
        )
    }

// ─────────────────────────────────────────────────────────────
// CompositionLocal
// ─────────────────────────────────────────────────────────────

/**
 * CompositionLocal providing the current [BasilColorScheme] down the composition tree.
 * Defaults to the day scheme; [BasilTheme] overrides this based on time of day.
 *
 * Prefer accessing colors via `MaterialTheme.basilColors` rather than reading this local directly.
 */
val LocalBasilColors = compositionLocalOf<BasilColorScheme> { basilDayColorScheme() }
