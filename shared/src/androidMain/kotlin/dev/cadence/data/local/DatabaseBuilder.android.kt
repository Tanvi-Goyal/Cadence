package dev.cadence.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Android's half of the DB-builder seam: Room needs an application [Context] to resolve the
 * on-device database file path. This is exactly why the builder can't live in commonMain.
 */
fun androidDatabaseBuilder(context: Context): RoomDatabase.Builder<AppDatabase> {
    val dbFile = context.getDatabasePath("cadence.db")
    return Room.databaseBuilder<AppDatabase>(
        context = context.applicationContext,
        name = dbFile.absolutePath,
    )
}
