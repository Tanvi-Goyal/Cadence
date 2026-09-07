package com.mindset.data.local

import android.content.Context
import android.content.pm.ApplicationInfo
import androidx.room3.Room
import androidx.room3.RoomDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.driver.bundled.BundledSQLiteDriver

/**
 * Android's half of the DB-builder seam: Room needs an application [Context] to resolve the
 * on-device database file path. This is exactly why the builder can't live in commonMain.
 *
 * Driver choice is build-type dependent: debuggable builds use the framework [AndroidSQLiteDriver]
 * so Android Studio's Database Inspector can attach (it hooks the framework SQLite connection and
 * cannot introspect the statically-linked [BundledSQLiteDriver] — doing so crashes with a native
 * SIGSEGV). Release keeps the version-pinned bundled driver, matching iOS and the shipping engine.
 */
fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("mindset.db")
    val debuggable = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    ).setDriver(if (debuggable) AndroidSQLiteDriver() else BundledSQLiteDriver())
}
