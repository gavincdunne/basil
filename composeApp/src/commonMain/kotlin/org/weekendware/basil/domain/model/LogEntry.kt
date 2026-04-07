package org.weekendware.basil.domain.model

data class LogEntry(
    val id: Long,
    val timestamp: Long,
    val bgValue: Double?,
    val bgUnit: BgUnit?,
    val insulinUnits: Double?,
    val carbsGrams: Double?
)
