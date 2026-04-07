package org.weekendware.basil.presentation.theme

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Basil's spacing scale — a fixed set of `Dp` values used for padding,
 * margins, gaps, and layout sizing throughout the app.
 *
 * Every spacing value in the UI should come from this scale. Using raw
 * `dp` literals in composables makes it hard to maintain visual consistency
 * and respond to layout changes (e.g. tablet or desktop breakpoints).
 *
 * ### Scale reference
 * | Token | Value | Typical use                                      |
 * |-------|-------|--------------------------------------------------|
 * | [xs]  | 4 dp  | Icon-to-label gaps, tight internal padding       |
 * | [sm]  | 8 dp  | List item vertical padding, chip padding         |
 * | [md]  | 12 dp | Form field vertical spacing                      |
 * | [lg]  | 16 dp | Standard horizontal screen margin               |
 * | [xl]  | 24 dp | Section vertical separation, card padding        |
 * | [xxl] | 32 dp | Large vertical breathing room, hero sections     |
 * | [xxxl]| 48 dp | Screen-level top/bottom offsets                  |
 *
 * A second instance with larger values can be provided via
 * [LocalBasilSpacing] for tablet or desktop layouts.
 *
 * @property xs   4 dp
 * @property sm   8 dp
 * @property md   12 dp
 * @property lg   16 dp
 * @property xl   24 dp
 * @property xxl  32 dp
 * @property xxxl 48 dp
 */
data class BasilSpacing(
    val xs:    Dp = 4.dp,
    val sm:    Dp = 8.dp,
    val md:    Dp = 12.dp,
    val lg:    Dp = 16.dp,
    val xl:    Dp = 24.dp,
    val xxl:   Dp = 32.dp,
    val xxxl:  Dp = 48.dp
)

/**
 * CompositionLocal that provides the current [BasilSpacing] instance.
 * Uses [staticCompositionLocalOf] because the default spacing scale does
 * not change at runtime (swap it for responsive layouts if needed).
 */
val LocalBasilSpacing = staticCompositionLocalOf { BasilSpacing() }
