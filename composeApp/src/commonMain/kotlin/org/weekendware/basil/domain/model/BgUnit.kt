package org.weekendware.basil.domain.model

/**
 * The unit used to express blood glucose concentration.
 *
 * @property label The human-readable display string shown in the UI.
 */
enum class BgUnit(val label: String) {
    /** Milligrams per decilitre — the standard unit in the US. */
    MGDL("mg/dL"),
    /** Millimoles per litre — used in the UK, Australia, Canada, and most of Europe. */
    MMOLL("mmol/L")
}
