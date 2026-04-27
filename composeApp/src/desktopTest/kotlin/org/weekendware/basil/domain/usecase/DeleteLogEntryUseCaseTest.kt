package org.weekendware.basil.domain.usecase

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.weekendware.basil.data.repository.SqlDelightLogRepository
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DeleteLogEntryUseCaseTest {

    private lateinit var repository: SqlDelightLogRepository
    private lateinit var saveUseCase: SaveLogEntryUseCase
    private lateinit var deleteUseCase: DeleteLogEntryUseCase

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BasilDatabase.Schema.create(driver)
        repository = SqlDelightLogRepository(BasilDatabase(driver))
        saveUseCase = SaveLogEntryUseCase(repository)
        deleteUseCase = DeleteLogEntryUseCase(repository)
    }

    @Test
    fun `deleting an entry removes it from the repository`() = runTest {
        saveUseCase("5.5", BgUnit.MMOLL, "", "")
        val entry = repository.getRecent(10).first().first()

        val result = deleteUseCase(entry.id)

        assertTrue(result.isSuccess)
        assertTrue(repository.getRecent(10).first().isEmpty())
    }

    @Test
    fun `deleting a non-existent id succeeds without error`() {
        val result = deleteUseCase(id = 999L)
        assertTrue(result.isSuccess)
    }

    @Test
    fun `deleting one of multiple entries leaves the others intact`() = runTest {
        saveUseCase("4.0", BgUnit.MMOLL, "", "")
        saveUseCase("8.0", BgUnit.MMOLL, "", "")
        val entries = repository.getRecent(10).first()
        val toDelete = entries.first()

        deleteUseCase(toDelete.id)

        val remaining = repository.getRecent(10).first()
        assertFalse(remaining.any { it.id == toDelete.id })
        assertTrue(remaining.size == 1)
    }
}
