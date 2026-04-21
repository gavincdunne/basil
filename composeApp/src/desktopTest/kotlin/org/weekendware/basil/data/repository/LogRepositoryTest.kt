package org.weekendware.basil.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.weekendware.basil.database.BasilDatabase
import org.weekendware.basil.domain.model.BgUnit
import org.weekendware.basil.data.repository.SqlDelightLogRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogRepositoryTest {

    private lateinit var repository: SqlDelightLogRepository

    @BeforeTest
    fun setup() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        BasilDatabase.Schema.create(driver)
        repository = SqlDelightLogRepository(BasilDatabase(driver))
    }

    // ── getRecent ─────────────────────────────────────────────

    @Test
    fun `getRecent returns empty list when no entries exist`() {
        assertTrue(repository.getRecent().isEmpty())
    }

    @Test
    fun `getRecent returns inserted entries`() {
        repository.insert(bgValue = 100.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)

        val entries = repository.getRecent()

        assertEquals(1, entries.size)
        assertEquals(100.0, entries.first().bgValue)
        assertEquals(BgUnit.MGDL, entries.first().bgUnit)
    }

    @Test
    fun `getRecent respects the limit parameter`() {
        repeat(10) { i ->
            repository.insert(bgValue = i.toDouble(), bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        }

        val entries = repository.getRecent(limit = 3)

        assertEquals(3, entries.size)
    }

    @Test
    fun `getRecent returns entries newest first`() {
        repository.insert(bgValue = 80.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        Thread.sleep(10) // ensure distinct timestamps
        repository.insert(bgValue = 120.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)

        val entries = repository.getRecent()

        assertEquals(120.0, entries.first().bgValue)
        assertEquals(80.0, entries.last().bgValue)
    }

    // ── insert ────────────────────────────────────────────────

    @Test
    fun `insert stores all nullable fields correctly`() {
        repository.insert(bgValue = 5.5, bgUnit = BgUnit.MMOLL, insulinUnits = 4.0, carbsGrams = 45.0)

        val entry = repository.getRecent().first()

        assertEquals(5.5,          entry.bgValue)
        assertEquals(BgUnit.MMOLL, entry.bgUnit)
        assertEquals(4.0,          entry.insulinUnits)
        assertEquals(45.0,         entry.carbsGrams)
    }

    @Test
    fun `insert stores entry with all nulls except timestamp`() {
        repository.insert(bgValue = null, bgUnit = null, insulinUnits = null, carbsGrams = null)

        val entry = repository.getRecent().first()

        assertNull(entry.bgValue)
        assertNull(entry.bgUnit)
        assertNull(entry.insulinUnits)
        assertNull(entry.carbsGrams)
        assertTrue(entry.timestamp > 0)
    }

    @Test
    fun `inserted entry receives an auto-incremented id`() {
        repository.insert(bgValue = 100.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        repository.insert(bgValue = 110.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)

        val entries = repository.getRecent()

        assertTrue(entries[0].id != entries[1].id)
    }

    // ── delete ────────────────────────────────────────────────

    @Test
    fun `delete removes the entry with the given id`() {
        repository.insert(bgValue = 100.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        val entry = repository.getRecent().first()
        assertNotNull(entry)

        repository.delete(entry.id)

        assertTrue(repository.getRecent().isEmpty())
    }

    @Test
    fun `delete only removes the targeted entry`() {
        repository.insert(bgValue = 100.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        repository.insert(bgValue = 120.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        val toDelete = repository.getRecent().first()

        repository.delete(toDelete.id)

        val remaining = repository.getRecent()
        assertEquals(1, remaining.size)
        assertTrue(remaining.none { it.id == toDelete.id })
    }

    // ── BgUnit round-trip ─────────────────────────────────────

    @Test
    fun `BgUnit MGDL survives insert and retrieve round-trip`() {
        repository.insert(bgValue = 100.0, bgUnit = BgUnit.MGDL, insulinUnits = null, carbsGrams = null)
        assertEquals(BgUnit.MGDL, repository.getRecent().first().bgUnit)
    }

    @Test
    fun `BgUnit MMOLL survives insert and retrieve round-trip`() {
        repository.insert(bgValue = 5.5, bgUnit = BgUnit.MMOLL, insulinUnits = null, carbsGrams = null)
        assertEquals(BgUnit.MMOLL, repository.getRecent().first().bgUnit)
    }
}
