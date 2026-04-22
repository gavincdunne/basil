package org.weekendware.basil.domain.usecase

import kotlinx.datetime.Clock
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.domain.model.LogEntry
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetTodayEntriesUseCaseTest {

    private val useCase = GetTodayEntriesUseCase()

    private fun makeEntry(id: Long, timestampOffset: Long = 0L) = LogEntry(
        id = id,
        timestamp = Clock.System.now().toEpochMilliseconds() - timestampOffset,
        bgValue = 100.0,
        bgUnit = BgUnit.MGDL,
        insulinUnits = null,
        carbsGrams = null
    )

    @Test
    fun `returns empty list when given empty input`() {
        assertTrue(useCase(emptyList()).isEmpty())
    }

    @Test
    fun `includes entries recorded today`() {
        val today = makeEntry(id = 1L)
        val result = useCase(listOf(today))
        assertEquals(1, result.size)
        assertEquals(1L, result.first().id)
    }

    @Test
    fun `excludes entries from yesterday`() {
        val yesterday = makeEntry(id = 1L, timestampOffset = 25 * 60 * 60 * 1000L)
        assertTrue(useCase(listOf(yesterday)).isEmpty())
    }

    @Test
    fun `returns only today entries from a mixed list`() {
        val today1 = makeEntry(id = 1L)
        val today2 = makeEntry(id = 2L)
        val yesterday = makeEntry(id = 3L, timestampOffset = 25 * 60 * 60 * 1000L)
        val result = useCase(listOf(today1, today2, yesterday))
        assertEquals(2, result.size)
        assertTrue(result.none { it.id == 3L })
    }
}
