package org.weekendware.basil.presentation.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * The root theme composable for the Basil app.
 *
 * Wrap the top-level [App] composable with [BasilTheme] to inject all design
 * tokens — colors, typography, shapes, and spacing — into the composition tree.
 *
 * ### How it works
 * [BasilTheme] does two things in parallel:
 * 1. Provides Basil-specific tokens via `CompositionLocal`s so that custom
 *    composables can access [MaterialTheme.basilColors], [MaterialTheme.basilSpacing],
 *    and [MaterialTheme.basilShapes].
 * 2. Configures Material3's [MaterialTheme] so that all standard Material
 *    components (Button, TextField, NavigationBar, etc.) automatically adopt
 *    the Basil palette, typography scale, and corner radii.
 *
 * ### Color scheme
 * The palette is determined by two independent signals: the local clock hour
 * (morning / day / evening / night) and the system dark-mode preference.
 * Pass `darkTheme = true` or a specific `hour` to override in previews or tests.
 *
 * ### Animated transitions
 * Every colour token is animated via [animateColorAsState] with a 10-second linear
 * easing. When the [hour] changes (driven by [BasilThemeViewModel] in production),
 * colours cross-fade smoothly. Previews use a fixed hour, so no animation fires.
 *
 * ### Customising tokens
 * - **Colors** → [BasilColors.kt] — edit the eight [basilSchemeForHour] variants.
 * - **Typography** → [BasilTypography.kt] — edit [basilTypography].
 * - **Shapes** → [BasilShapes.kt] — edit the defaults on [BasilShapes].
 * - **Spacing** → [BasilSpacing.kt] — edit the defaults on [BasilSpacing].
 * - **Component sizes/elevations** → [BasilTokens.kt].
 *
 * @param darkTheme Whether to use the dark variant. Defaults to the system setting.
 * @param hour     Current hour (0–23). Defaults to the device clock. Override in previews.
 * @param content  The composable tree to theme.
 */
@Composable
fun BasilTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    hour: Int = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).hour,
    content: @Composable () -> Unit
) {
    val target = basilSchemeForHour(hour, isDark = darkTheme)
    val colors = animatedBasilColorScheme(target)
    val shapes = BasilShapes()

    CompositionLocalProvider(
        LocalBasilColors  provides colors,
        LocalBasilSpacing provides BasilSpacing(),
        LocalBasilShapes  provides shapes
    ) {
        MaterialTheme(
            colorScheme = colors.toMaterialColorScheme(),
            typography  = basilTypography(),
            shapes      = shapes.toMaterialShapes()
        ) {
            content()
        }
    }
}

/**
 * Animates every colour token in [target] independently over 10 seconds (linear easing).
 *
 * When [target] changes, each [animateColorAsState] starts from the currently rendered
 * colour value and interpolates to the new one — no hard cuts, no recomposition of the
 * content tree. In previews the target never changes, so no animation fires.
 */
@Composable
private fun animatedBasilColorScheme(target: BasilColorScheme): BasilColorScheme {
    val spec = tween<Color>(durationMillis = 10_000, easing = LinearEasing)

    val primary                  by animateColorAsState(target.primary,                  spec, label = "primary")
    val onPrimary                by animateColorAsState(target.onPrimary,                spec, label = "onPrimary")
    val primaryContainer         by animateColorAsState(target.primaryContainer,         spec, label = "primaryContainer")
    val onPrimaryContainer       by animateColorAsState(target.onPrimaryContainer,       spec, label = "onPrimaryContainer")
    val secondary                by animateColorAsState(target.secondary,                spec, label = "secondary")
    val onSecondary              by animateColorAsState(target.onSecondary,              spec, label = "onSecondary")
    val secondaryContainer       by animateColorAsState(target.secondaryContainer,       spec, label = "secondaryContainer")
    val onSecondaryContainer     by animateColorAsState(target.onSecondaryContainer,     spec, label = "onSecondaryContainer")
    val background               by animateColorAsState(target.background,               spec, label = "background")
    val onBackground             by animateColorAsState(target.onBackground,             spec, label = "onBackground")
    val surface                  by animateColorAsState(target.surface,                  spec, label = "surface")
    val onSurface                by animateColorAsState(target.onSurface,                spec, label = "onSurface")
    val surfaceVariant           by animateColorAsState(target.surfaceVariant,           spec, label = "surfaceVariant")
    val onSurfaceVariant         by animateColorAsState(target.onSurfaceVariant,         spec, label = "onSurfaceVariant")
    val error                    by animateColorAsState(target.error,                    spec, label = "error")
    val onError                  by animateColorAsState(target.onError,                  spec, label = "onError")
    val errorContainer           by animateColorAsState(target.errorContainer,           spec, label = "errorContainer")
    val onErrorContainer         by animateColorAsState(target.onErrorContainer,         spec, label = "onErrorContainer")
    val outline                  by animateColorAsState(target.outline,                  spec, label = "outline")
    val outlineVariant           by animateColorAsState(target.outlineVariant,           spec, label = "outlineVariant")
    val scrim                    by animateColorAsState(target.scrim,                    spec, label = "scrim")
    val warmAccent               by animateColorAsState(target.warmAccent,               spec, label = "warmAccent")
    val onWarmAccent             by animateColorAsState(target.onWarmAccent,             spec, label = "onWarmAccent")
    val backgroundGradientTop    by animateColorAsState(target.backgroundGradientTop,    spec, label = "gradientTop")
    val backgroundGradientBottom by animateColorAsState(target.backgroundGradientBottom, spec, label = "gradientBottom")

    return BasilColorScheme(
        primary                  = primary,
        onPrimary                = onPrimary,
        primaryContainer         = primaryContainer,
        onPrimaryContainer       = onPrimaryContainer,
        secondary                = secondary,
        onSecondary              = onSecondary,
        secondaryContainer       = secondaryContainer,
        onSecondaryContainer     = onSecondaryContainer,
        background               = background,
        onBackground             = onBackground,
        surface                  = surface,
        onSurface                = onSurface,
        surfaceVariant           = surfaceVariant,
        onSurfaceVariant         = onSurfaceVariant,
        error                    = error,
        onError                  = onError,
        errorContainer           = errorContainer,
        onErrorContainer         = onErrorContainer,
        outline                  = outline,
        outlineVariant           = outlineVariant,
        scrim                    = scrim,
        isDark                   = target.isDark,
        warmAccent               = warmAccent,
        onWarmAccent             = onWarmAccent,
        backgroundGradientTop    = backgroundGradientTop,
        backgroundGradientBottom = backgroundGradientBottom,
    )
}

// ─────────────────────────────────────────────────────────────
// MaterialTheme extension accessors
//
// Use these in composables instead of reading the CompositionLocals
// directly — they mirror the `MaterialTheme.colorScheme` pattern and
// make call sites consistent and easy to read.
// ─────────────────────────────────────────────────────────────

/**
 * The current [BasilColorScheme] from the composition.
 *
 * Usage: `MaterialTheme.basilColors.primary`
 */
val MaterialTheme.basilColors: BasilColorScheme
    @Composable @ReadOnlyComposable
    get() = LocalBasilColors.current

/**
 * The current [BasilSpacing] scale from the composition.
 *
 * Usage: `MaterialTheme.basilSpacing.lg`
 */
val MaterialTheme.basilSpacing: BasilSpacing
    @Composable @ReadOnlyComposable
    get() = LocalBasilSpacing.current

/**
 * The current [BasilShapes] from the composition.
 *
 * Usage: `MaterialTheme.basilShapes.md`
 */
val MaterialTheme.basilShapes: BasilShapes
    @Composable @ReadOnlyComposable
    get() = LocalBasilShapes.current
