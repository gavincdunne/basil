package org.weekendware.basil.data.local.database

import org.weekendware.basil.database.BasilDatabase

/**
 * Singleton factory for the [BasilDatabase] instance.
 *
 * Ensures the database is created exactly once per process lifetime,
 * regardless of how many times [getDatabase] is called. The instance is
 * lazily initialised on the first call and reused thereafter.
 *
 * This is intentionally a plain object (not a Koin `single`) so it can
 * be called from the platform-specific DI setup before Koin is fully
 * configured. In practice, Koin's `databaseModule` uses this object
 * to obtain the shared instance.
 */
object DatabaseProvider {

    private var instance: BasilDatabase? = null

    /**
     * Returns the singleton [BasilDatabase], creating it if necessary.
     *
     * Thread safety is not explicitly enforced here; this should be called
     * from the main thread during app startup (as Koin's DI graph is built).
     *
     * @param driverFactory Platform-specific [DatabaseDriverFactory] used
     *   to create the underlying [SqlDriver] on first call.
     * @return The shared [BasilDatabase] instance.
     */
    fun getDatabase(driverFactory: DatabaseDriverFactory): BasilDatabase {
        if (instance == null) {
            instance = BasilDatabase(driverFactory.createDriver())
        }
        return instance!!
    }
}
