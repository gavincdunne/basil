package org.weekendware.basil.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

/**
 * Basil's shape tokens, defining the corner radii used throughout the UI.
 *
 * ### Guidelines
 * - **[xs]** — inline chips, badges, small tags
 * - **[sm]** — input fields, small cards
 * - **[md]** — primary cards, list tiles
 * - **[lg]** — sheets, dialogs, large cards
 * - **[xl]** — prominent containers, hero cards
 * - **[full]** — FABs, avatar circles, toggle pills
 *
 * Access via [LocalBasilShapes].current or `MaterialTheme.basilShapes`.
 *
 * @property xs  4 dp — smallest rounding, tight elements.
 * @property sm  8 dp — inputs, small surface elements.
 * @property md  12 dp — standard cards and tiles.
 * @property lg  16 dp — sheets and dialogs.
 * @property xl  24 dp — large feature cards.
 * @property full Circle — fully-rounded pill or circular shapes.
 */
data class BasilShapes(
    val xs:   Shape = RoundedCornerShape(4.dp),
    val sm:   Shape = RoundedCornerShape(8.dp),
    val md:   Shape = RoundedCornerShape(12.dp),
    val lg:   Shape = RoundedCornerShape(16.dp),
    val xl:   Shape = RoundedCornerShape(24.dp),
    val full: Shape = CircleShape
)

/**
 * Maps [BasilShapes] to Material3's [Shapes] so that standard Material
 * components pick up the correct corner radii automatically.
 */
internal fun BasilShapes.toMaterialShapes(): Shapes = Shapes(
    extraSmall = xs as RoundedCornerShape,
    small      = sm as RoundedCornerShape,
    medium     = md as RoundedCornerShape,
    large      = lg as RoundedCornerShape,
    extraLarge = xl as RoundedCornerShape
)

/**
 * CompositionLocal that provides the current [BasilShapes] instance.
 * Uses [staticCompositionLocalOf] because shapes do not change at runtime.
 */
val LocalBasilShapes = staticCompositionLocalOf { BasilShapes() }
