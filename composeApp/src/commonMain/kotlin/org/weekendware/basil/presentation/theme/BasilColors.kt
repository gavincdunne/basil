package org.weekendware.basil.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.Brush
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

    // Auth screens — literal, hand-picked by UI Designer, not derived from
    // the seed palette. Same treatment as Cream above.
    val AuthFieldBorder     = Color(0xFF858078)
    val AuthFieldText       = Color(0xFF4E6A50)
    val AuthButtonMuted     = Color(0xFFA8BAA5)
    val AuthGoogleBorder    = Color(0xFFE8E4DF)
    val AuthInfoCardBg      = Color(0xFFD4E8D6)
    val AuthPlaceholderText = Color(0xFFB8B0A8)
    val AuthNearBlack       = Color(0xFF1A1816)
    /** Amber strength-bar segment / label — [Error] doubles as the red segment. */
    val AuthStrengthAmber = Color(0xFFE07A10)
    /** Green strength-bar segment / label and the strong-password field border. */
    val AuthStrengthGreen = Color(0xFF2E7D32)
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
    // Vertical gradient endpoints for the full-screen background.
    // When both equal background the result is a flat fill.
    val backgroundGradientTop: Color = background,
    val backgroundGradientBottom: Color = background,
)

/** Returns a top-to-bottom gradient brush for the screen background. */
fun BasilColorScheme.backgroundBrush(): Brush =
    Brush.verticalGradient(colors = listOf(backgroundGradientTop, backgroundGradientBottom))

// ─────────────────────────────────────────────────────────────
// Day/night scheme constructors
// ─────────────────────────────────────────────────────────────

/**
 * Morning scheme (5am–10am). Warm wheat-gold: dawn light through dew.
 * Gradient top #DDD0A0 → bottom #F8F3E4. Chrome #F5EDD0, borders #C8A84A.
 */
fun basilMorningColorScheme(): BasilColorScheme = BasilColorScheme(
    primary                  = Color(0xFF546857), // constant sage — user bubbles, send btn
    onPrimary                = Color(0xFFFFFFFF),
    primaryContainer         = Color(0xFFC8D4A8), // nav active indicator pill
    onPrimaryContainer       = Color(0xFF546857),
    secondary                = Color(0xFF706020), // inactive nav icons
    onSecondary              = Color(0xFFFFFFFF),
    secondaryContainer       = Color(0xFFFDFBF0), // Basil bubble background
    onSecondaryContainer     = Color(0xFF2C2000), // Basil bubble text
    background               = Color(0xFFF8F3E4),
    onBackground             = Color(0xFF2C2000),
    surface                  = Color(0xFFF5EDD0), // top bar, nav, input bar
    onSurface                = Color(0xFF2C2000),
    surfaceVariant           = Color(0xFFFDFBF0), // input field background
    onSurfaceVariant         = Color(0xFF706020), // subtext, cog, secondary labels
    error                    = BasilPalette.Error,
    onError                  = BasilPalette.OnError,
    errorContainer           = BasilPalette.ErrorContainer,
    onErrorContainer         = BasilPalette.OnErrorContainer,
    outline                  = Color(0xFFC8A84A), // bubble borders, field borders
    outlineVariant           = Color(0xFFC8A84A), // structural dividers
    scrim                    = BasilPalette.Scrim,
    isDark                   = false,
    backgroundGradientTop    = Color(0xFFDDD0A0),
    backgroundGradientBottom = Color(0xFFF8F3E4),
)

/**
 * Day scheme (10am–6pm). Barely-green wash to cream — just enough to say
 * "plant" without saying it. Gradient top #DFF0E4 → bottom #FAF8F5.
 */
fun basilDayColorScheme(): BasilColorScheme = BasilColorScheme(
    primary                  = Color(0xFF546857),
    onPrimary                = Color(0xFFFFFFFF),
    primaryContainer         = Color(0xFFD4E8D6), // nav active indicator pill
    onPrimaryContainer       = Color(0xFF546857),
    secondary                = Color(0xFF7A9478), // inactive nav icons
    onSecondary              = Color(0xFFFFFFFF),
    secondaryContainer       = Color(0xFFFFFFFF), // Basil bubble background
    onSecondaryContainer     = Color(0xFF1A1816),
    background               = Color(0xFFFAF8F5),
    onBackground             = Color(0xFF1A1816),
    surface                  = Color(0xFFFFFFFF), // top bar, nav, input bar
    onSurface                = Color(0xFF1A1816),
    surfaceVariant           = Color(0xFFFAF8F5), // input field background
    onSurfaceVariant         = Color(0xFF4E6A50), // subtext, cog, secondary labels
    error                    = BasilPalette.Error,
    onError                  = BasilPalette.OnError,
    errorContainer           = BasilPalette.ErrorContainer,
    onErrorContainer         = BasilPalette.OnErrorContainer,
    outline                  = Color(0xFF858078),
    outlineVariant           = Color(0xFF858078),
    scrim                    = BasilPalette.Scrim,
    isDark                   = false,
    backgroundGradientTop    = Color(0xFFDFF0E4),
    backgroundGradientBottom = Color(0xFFFAF8F5),
)

/**
 * Evening scheme (6pm–9pm). Olive-gold: golden hour light through foliage.
 * Gradient top #C4BA4C → bottom #F0E8BC. Chrome #EEE5B0, borders #A89830.
 */
fun basilEveningColorScheme(): BasilColorScheme = BasilColorScheme(
    primary                  = Color(0xFF546857),
    onPrimary                = Color(0xFFFFFFFF),
    primaryContainer         = Color(0xFFC8D090), // nav active indicator pill (olive)
    onPrimaryContainer       = Color(0xFF405440),
    secondary                = Color(0xFF605810), // inactive nav icons
    onSecondary              = Color(0xFFFFFFFF),
    secondaryContainer       = Color(0xFFFDFAE8), // Basil bubble background
    onSecondaryContainer     = Color(0xFF1A1800),
    background               = Color(0xFFF0E8BC),
    onBackground             = Color(0xFF1A1800),
    surface                  = Color(0xFFEEE5B0), // top bar, nav, input bar
    onSurface                = Color(0xFF1A1800),
    surfaceVariant           = Color(0xFFFDFAE8), // input field background
    onSurfaceVariant         = Color(0xFF605810), // subtext, cog, secondary labels
    error                    = BasilPalette.Error,
    onError                  = BasilPalette.OnError,
    errorContainer           = BasilPalette.ErrorContainer,
    onErrorContainer         = BasilPalette.OnErrorContainer,
    outline                  = Color(0xFFA89830),
    outlineVariant           = Color(0xFFA89830),
    scrim                    = BasilPalette.Scrim,
    isDark                   = false,
    warmAccent               = Color(0xFFA89830),
    onWarmAccent             = Color(0xFF1A1800),
    backgroundGradientTop    = Color(0xFFC4BA4C),
    backgroundGradientBottom = Color(0xFFF0E8BC),
)

/**
 * Night scheme (9pm–5am). Deep forest: the plant comes fully home at night.
 * Gradient top #0C1A0E → bottom #162418. Chrome #131E14, dividers #1E3020.
 */
fun basilNightColorScheme(): BasilColorScheme = BasilColorScheme(
    primary                  = Color(0xFF8FA08B), // muted luminous sage
    onPrimary                = Color(0xFF0C1A0E),
    primaryContainer         = Color(0x3F8FA08B), // nav active pill at 25% opacity
    onPrimaryContainer       = Color(0xFF8FA08B),
    secondary                = Color(0xFF6A9470), // inactive nav icons
    onSecondary              = Color(0xFF0C1A0E),
    secondaryContainer       = Color(0xFF141E16), // Basil bubble background
    onSecondaryContainer     = Color(0xFFB8D4B8), // Basil bubble text
    background               = Color(0xFF0C1A0E),
    onBackground             = Color(0xFFB8D4B8),
    surface                  = Color(0xFF131E14), // top bar, nav, input bar
    onSurface                = Color(0xFF8FA08B),
    surfaceVariant           = Color(0xFF141E16),
    onSurfaceVariant         = Color(0xFF6A9470), // subtext, cog, secondary labels
    error                    = Color(0xFFFFB4AB),
    onError                  = Color(0xFF690005),
    errorContainer           = Color(0xFF93000A),
    onErrorContainer         = Color(0xFFFFDAD6),
    outline                  = Color(0xFF5A8060), // bubble borders, field borders
    outlineVariant           = Color(0xFF1E3020), // structural dividers (subtle)
    scrim                    = BasilPalette.Scrim,
    isDark                   = true,
    backgroundGradientTop    = Color(0xFF0C1A0E),
    backgroundGradientBottom = Color(0xFF162418),
)

// ─────────────────────────────────────────────────────────────
// Dark-mode counterparts (system dark theme active)
//
// Each time slot has a dark variant. The hue character and warmth
// of the time slot survive — only brightness shifts down.
// ─────────────────────────────────────────────────────────────

/**
 * Morning dark (5am–10am, system dark). Dark amber-forest: the warm amber
 * character of dawn survives against a deep background.
 */
fun basilMorningDarkColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilSagePalette.shade300,
    onPrimary            = BasilSagePalette.shade900,
    primaryContainer     = BasilSagePalette.shade700,
    onPrimaryContainer   = BasilSagePalette.shade100,
    secondary            = BasilStonePalette.shade300,
    onSecondary          = BasilStonePalette.shade900,
    secondaryContainer   = BasilStonePalette.shade700,
    onSecondaryContainer = BasilStonePalette.shade100,
    background           = hslToColor(35f, 0.22f, 0.09f),  // deep amber-forest
    onBackground         = hslToColor(35f, 0.30f, 0.90f),  // warm near-white
    surface              = hslToColor(35f, 0.18f, 0.12f),
    onSurface            = hslToColor(35f, 0.28f, 0.88f),
    surfaceVariant       = hslToColor(35f, 0.14f, 0.16f),
    onSurfaceVariant     = hslToColor(35f, 0.12f, 0.60f),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = hslToColor(35f, 0.18f, 0.30f),
    outlineVariant       = hslToColor(35f, 0.14f, 0.20f),
    scrim                = BasilPalette.Scrim,
    isDark               = true,
    warmAccent           = BasilAmberPalette.shade400,
    onWarmAccent         = BasilPalette.Stone900,
)

/**
 * Day dark (10am–6pm, system dark). Cool dark forest: midday quality
 * palette, just dark. Clean sage-green character with no warm tint.
 */
fun basilDayDarkColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilSagePalette.shade300,
    onPrimary            = BasilSagePalette.shade900,
    primaryContainer     = BasilSagePalette.shade700,
    onPrimaryContainer   = BasilSagePalette.shade100,
    secondary            = BasilStonePalette.shade300,
    onSecondary          = BasilStonePalette.shade900,
    secondaryContainer   = BasilStonePalette.shade700,
    onSecondaryContainer = BasilStonePalette.shade100,
    background           = hslToColor(130f, 0.09f, 0.09f), // dark cool forest
    onBackground         = BasilStonePalette.shade100,
    surface              = BasilStonePalette.shade800,
    onSurface            = BasilStonePalette.shade100,
    surfaceVariant       = BasilStonePalette.shade700,
    onSurfaceVariant     = BasilStonePalette.shade300,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = BasilStonePalette.shade500,
    outlineVariant       = BasilStonePalette.shade700,
    scrim                = BasilPalette.Scrim,
    isDark               = true,
)

/**
 * Evening dark (6pm–9pm, system dark). Rich deep olive-forest: the golden-olive
 * warmth of dusk compressed into darkness. Amber accent survives.
 */
fun basilEveningDarkColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilSagePalette.shade300,
    onPrimary            = BasilSagePalette.shade900,
    primaryContainer     = BasilSagePalette.shade700,
    onPrimaryContainer   = BasilSagePalette.shade100,
    secondary            = BasilStonePalette.shade300,
    onSecondary          = BasilStonePalette.shade900,
    secondaryContainer   = BasilStonePalette.shade700,
    onSecondaryContainer = BasilStonePalette.shade100,
    background           = hslToColor(75f, 0.15f, 0.09f),  // deep olive-forest
    onBackground         = hslToColor(40f, 0.25f, 0.90f),  // warm-gold near-white
    surface              = hslToColor(75f, 0.12f, 0.12f),
    onSurface            = hslToColor(40f, 0.22f, 0.88f),
    surfaceVariant       = hslToColor(75f, 0.09f, 0.16f),
    onSurfaceVariant     = hslToColor(40f, 0.10f, 0.62f),
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = hslToColor(75f, 0.14f, 0.30f),
    outlineVariant       = hslToColor(75f, 0.10f, 0.20f),
    scrim                = BasilPalette.Scrim,
    isDark               = true,
    warmAccent           = BasilAmberPalette.shade300,
    onWarmAccent         = BasilPalette.Stone900,
)

/**
 * Night dark (9pm–5am, system dark). Deepest forest: the floor of the forest
 * at its most compressed. Barely any hue remains — just depth.
 */
fun basilNightDarkColorScheme(): BasilColorScheme = BasilColorScheme(
    primary              = BasilSagePalette.shade200,
    onPrimary            = BasilSagePalette.shade900,
    primaryContainer     = BasilSagePalette.shade800,
    onPrimaryContainer   = BasilSagePalette.shade100,
    secondary            = BasilStonePalette.shade200,
    onSecondary          = BasilStonePalette.shade900,
    secondaryContainer   = BasilStonePalette.shade800,
    onSecondaryContainer = BasilStonePalette.shade100,
    background           = hslToColor(130f, 0.06f, 0.07f), // deepest forest
    onBackground         = BasilStonePalette.shade100,
    surface              = hslToColor(130f, 0.05f, 0.09f),
    onSurface            = BasilStonePalette.shade100,
    surfaceVariant       = hslToColor(130f, 0.04f, 0.12f),
    onSurfaceVariant     = BasilStonePalette.shade300,
    error                = Color(0xFFFFB4AB),
    onError              = Color(0xFF690005),
    errorContainer       = Color(0xFF93000A),
    onErrorContainer     = Color(0xFFFFDAD6),
    outline              = BasilStonePalette.shade600,
    outlineVariant       = hslToColor(130f, 0.04f, 0.16f),
    scrim                = BasilPalette.Scrim,
    isDark               = true,
)

// ─────────────────────────────────────────────────────────────
// Time-of-day scheme selector
//
// Call basilSchemeForHour(hour, isDark) in BasilTheme.
// isDark comes from isSystemInDarkTheme() — it is independent of
// the time slot. A user at noon with dark mode on still sees the
// midday character palette, just dark.
// ─────────────────────────────────────────────────────────────

/**
 * Returns the appropriate [BasilColorScheme] for the given [hour] (0–23)
 * and [isDark] system preference.
 */
fun basilSchemeForHour(hour: Int, isDark: Boolean = false): BasilColorScheme = when (hour) {
    in 5..9   -> if (isDark) basilMorningDarkColorScheme() else basilMorningColorScheme()
    in 10..17 -> if (isDark) basilDayDarkColorScheme()     else basilDayColorScheme()
    in 18..20 -> if (isDark) basilEveningDarkColorScheme() else basilEveningColorScheme()
    else      -> if (isDark) basilNightDarkColorScheme()   else basilNightColorScheme()
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
