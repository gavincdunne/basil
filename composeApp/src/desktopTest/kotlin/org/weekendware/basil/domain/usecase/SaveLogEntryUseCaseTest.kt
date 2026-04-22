package org.weekendware.basil.domain.usecase

import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.weekendware.basil.data.repository.LogRepository
import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SaveLogEntryUseCaseTest {

    private val logRepository = mock<LogRepository>()
    private val useCase = SaveLogEntryUseCase(logRepository)

    @Test
    fun `returns false and does not insert when all fields are blank`() {
        val result = useCase(bgValue = "", bgUnit = BgUnit.MGDL, insulinUnits = "", carbsGrams = "")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        verifyNoInteractions(logRepository)
    }

    @Test
    fun `returns true and inserts when bgValue is provided`() {
        val result = useCase(bgValue = "120", bgUnit = BgUnit.MGDL, insulinUnits = "", carbsGrams = "")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        verify(logRepository).insert(bgValue = 120.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
    }

    @Test
    fun `returns true and inserts when insulinUnits is provided`() {
        val result = useCase(bgValue = "", bgUnit = BgUnit.MGDL, insulinUnits = "4", carbsGrams = "")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        verify(logRepository).insert(bgValue = null, bgUnit = null, insulinUnits = 4.0, carbsGrams = null)
    }

    @Test
    fun `returns true and inserts when carbsGrams is provided`() {
        val result = useCase(bgValue = "", bgUnit = BgUnit.MGDL, insulinUnits = "", carbsGrams = "45")

        assertTrue(result.isSuccess)
        assertTrue(result.getOrThrow())
        verify(logRepository).insert(bgValue = null, bgUnit = null, insulinUnits = null, carbsGrams = 45.0)
    }

    @Test
    fun `sets bgUnit to null when bgValue is blank`() {
        useCase(bgValue = "", bgUnit = BgUnit.MMOLL, insulinUnits = "3", carbsGrams = "")

        verify(logRepository).insert(bgValue = null, bgUnit = null, insulinUnits = 3.0, carbsGrams = null)
    }

    @Test
    fun `passes all fields correctly for a full entry`() {
        useCase(bgValue = "5.5", bgUnit = BgUnit.MMOLL, insulinUnits = "4", carbsGrams = "45")

        verify(logRepository).insert(
            bgValue = 5.5,
            bgUnit = BgUnit.MMOLL,
            insulinUnits = 4.0,
            carbsGrams = 45.0
        )
    }

    @Test
    fun `ignores non-numeric bgValue and treats entry as no-op`() {
        val result = useCase(bgValue = "abc", bgUnit = BgUnit.MGDL, insulinUnits = "", carbsGrams = "")

        assertTrue(result.isSuccess)
        assertFalse(result.getOrThrow())
        verifyNoInteractions(logRepository)
    }
}
