package org.weekendware.basil.presentation.theme

import androidx.compose.ui.unit.dp

/**
 * Component-level design tokens for Basil.
 *
 * These are concrete size and elevation values for specific UI components.
 * When a component has a unique sizing requirement that doesn't belong in
 * [BasilSpacing] or [BasilShapes], it lives here so there is one place to
 * adjust it across the app.
 *
 * ### Organisation
 * Tokens are grouped by the component they belong to. Add new groups as new
 * components are introduced; remove groups when components are deleted.
 */
object BasilTokens {

    // ── Top App Bar ───────────────────────────────────────────
    /** Height of the [BasilTopAppBar]. */
    val TopBarHeight = 64.dp

    // ── Bottom Navigation Bar ─────────────────────────────────
    /** Height of the [BasilBottomBar]. */
    val BottomBarHeight = 80.dp

    // ── Floating Action Button ────────────────────────────────
    /** Standard FAB size. */
    val FabSize = 56.dp
    /** Padding from the screen edge to the FAB. */
    val FabEdgePadding = 16.dp

    // ── Cards ─────────────────────────────────────────────────
    /** Elevation for standard content cards. */
    val CardElevation = 1.dp
    /** Elevation for cards in a pressed / focused state. */
    val CardPressedElevation = 4.dp

    // ── Bottom Sheet ──────────────────────────────────────────
    /** Horizontal content padding inside bottom sheets. */
    val SheetHorizontalPadding = 24.dp
    /** Bottom padding inside bottom sheets (above home indicator). */
    val SheetBottomPadding = 32.dp

    // ── Form / Inputs ─────────────────────────────────────────
    /** Standard height for [OutlinedTextField] inputs. */
    val InputHeight = 56.dp
    /** Vertical gap between consecutive form fields. */
    val FormFieldGap = 20.dp

    // ── Buttons ───────────────────────────────────────────────
    /** Height for full-width action buttons. */
    val ButtonHeight = 52.dp

    // ── Icons ─────────────────────────────────────────────────
    val IconSizeSm = 16.dp
    val IconSizeMd = 24.dp
    val IconSizeLg = 32.dp

    // ── Splash screen ─────────────────────────────────────────
    /** Size of the frosted icon container on the splash screen. */
    val SplashIconContainerSize = 88.dp
    /** Corner radius of the splash screen icon container. */
    val SplashIconContainerCorner = 24.dp
    /** Size of the leaf mark on the splash screen. */
    val SplashLeafSize = 48.dp

    // ── Auth screen ───────────────────────────────────────────
    /** Corner radius of the hero band's bottom edge on the auth screen. */
    val AuthHeroCorner = 32.dp
    /** Size of the leaf mark inside the auth hero band. */
    val AuthHeroLeafSize = 38.dp

    // ── Glucose indicator ─────────────────────────────────────
    /** Diameter of the dot used in glucose trend indicators. */
    val GlucoseDotSize = 12.dp
    /** Height of the compact glucose status strip on the dashboard. */
    val GlucoseStripHeight = 48.dp
}
