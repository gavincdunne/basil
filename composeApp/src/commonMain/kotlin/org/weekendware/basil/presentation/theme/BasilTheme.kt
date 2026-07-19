package org.weekendware.basil.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
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
    val colors = basilSchemeForHour(hour, isDark = darkTheme)
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
