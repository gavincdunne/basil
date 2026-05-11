package org.weekendware.basil.domain.usecase

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.weekendware.basil.data.repository.SqlDelightLogRepository
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ObserveRecentLogsUseCaseTest {

    private lateinit var repository: SqlDelightLogRepository
    private lateinit var saveUseCase: SaveLogEntryUseCase
    private lateinit var observeUseCase: ObserveRecentLogsUseCase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BasilDatabase.Schema.create(driver)
        repository = SqlDelightLogRepository(BasilDatabase(driver))
        saveUseCase = SaveLogEntryUseCase(repository)
        observeUseCase = ObserveRecentLogsUseCase(repository)
    }

    @Test
    fun `emits empty list when no entries exist`() = runTest {
        assertTrue(observeUseCase().first().isEmpty())
    }

    @Test
    fun `emits saved entries`() = runTest {
        saveUseCase("7.2", BgUnit.MMOLL, "4", "60")
        val entries = observeUseCase().first()
        assertEquals(1, entries.size)
    }

    @Test
    fun `entries are ordered most recent first`() = runTest {
        saveUseCase("4.0", BgUnit.MMOLL, "", "")
        saveUseCase("9.0", BgUnit.MMOLL, "", "")
        val entries = observeUseCase().first()
        assertTrue(entries[0].timestamp >= entries[1].timestamp)
    }

    @Test
    fun `respects the 100-entry limit`() = runTest {
        repeat(105) { saveUseCase("5.0", BgUnit.MMOLL, "", "") }
        val entries = observeUseCase().first()
        assertEquals(100, entries.size)
    }
}
