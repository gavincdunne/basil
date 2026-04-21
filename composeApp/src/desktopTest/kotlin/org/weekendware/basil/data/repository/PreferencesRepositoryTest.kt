package org.weekendware.basil.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class PreferencesRepositoryTest {

    private lateinit var repository: PreferencesRepository

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BasilDatabase.Schema.create(driver)
        repository = PreferencesRepository(BasilDatabase(driver))
    }

    // ── getBgUnit ─────────────────────────────────────────────

    @Test
    fun `getBgUnit returns MGDL when no preference has been set`() {
        assertEquals(BgUnit.MGDL, repository.getBgUnit())
    }

    // ── setBgUnit / getBgUnit round-trip ──────────────────────

    @Test
    fun `setBgUnit persists MGDL and getBgUnit retrieves it`() {
        repository.setBgUnit(BgUnit.MGDL)
        assertEquals(BgUnit.MGDL, repository.getBgUnit())
    }

    @Test
    fun `setBgUnit persists MMOLL and getBgUnit retrieves it`() {
        repository.setBgUnit(BgUnit.MMOLL)
        assertEquals(BgUnit.MMOLL, repository.getBgUnit())
    }

    @Test
    fun `setBgUnit overwrites the previous preference`() {
        repository.setBgUnit(BgUnit.MGDL)
        repository.setBgUnit(BgUnit.MMOLL)

        assertEquals(BgUnit.MMOLL, repository.getBgUnit())
    }

    @Test
    fun `setBgUnit can toggle back to MGDL`() {
        repository.setBgUnit(BgUnit.MMOLL)
        repository.setBgUnit(BgUnit.MGDL)

        assertEquals(BgUnit.MGDL, repository.getBgUnit())
    }
}
