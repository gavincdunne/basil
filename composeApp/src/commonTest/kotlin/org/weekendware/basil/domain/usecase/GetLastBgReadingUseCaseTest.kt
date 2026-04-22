package org.weekendware.basil.domain.usecase

import kotlinx.datetime.Clock
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class GetLastBgReadingUseCaseTest {

    private val useCase = GetLastBgReadingUseCase()

    private fun makeEntry(id: Long, bgValue: Double? = null, insulinUnits: Double? = null) = LogEntry(
        id = id,
        timestamp = Clock.System.now().toEpochMilliseconds(),
        bgValue = bgValue,
        bgUnit = if (bgValue != null) BgUnit.MGDL else null,
        insulinUnits = insulinUnits,
        carbsGrams = null
    )

    @Test
    fun `returns null for empty list`() {
        assertNull(useCase(emptyList()))
    }

    @Test
    fun `returns null when no entry has a bgValue`() {
        val entries = listOf(makeEntry(id = 1L, insulinUnits = 4.0))
        assertNull(useCase(entries))
    }

    @Test
    fun `returns first entry with a bgValue`() {
        val withBg = makeEntry(id = 1L, bgValue = 120.0)
        val withoutBg = makeEntry(id = 2L, insulinUnits = 4.0)
        assertEquals(1L, useCase(listOf(withBg, withoutBg))?.id)
    }

    @Test
    fun `skips leading non-BG entries to find the first BG reading`() {
        val noBg = makeEntry(id = 1L, insulinUnits = 4.0)
        val withBg = makeEntry(id = 2L, bgValue = 95.0)
        assertEquals(2L, useCase(listOf(noBg, withBg))?.id)
    }

    @Test
    fun `returns the first entry when multiple entries have bgValues`() {
        val first = makeEntry(id = 1L, bgValue = 120.0)
        val second = makeEntry(id = 2L, bgValue = 80.0)
        assertEquals(1L, useCase(listOf(first, second))?.id)
    }
}
