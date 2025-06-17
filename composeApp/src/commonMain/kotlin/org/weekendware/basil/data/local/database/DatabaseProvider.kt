package org.weekendware.basil.data.local.database

import org.weekendware.basil.database.BasilDatabase

object DatabaseProvider {
    private var instance: BasilDatabase? = null

    fun getDatabase(driverFactory: DatabaseDriverFactory): BasilDatabase {
        if (instance == null) {
            val driver = driverFactory.createDriver()
            instance = BasilDatabase(driver)
        }
        return instance!!
    }
}