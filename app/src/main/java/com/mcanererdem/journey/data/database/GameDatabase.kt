package com.mcanererdem.journey.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mcanererdem.journey.data.dao.GameDao
import com.mcanererdem.journey.data.model.JournalEntry
import com.mcanererdem.journey.data.model.PlayerProfile

@Database(entities = [PlayerProfile::class, JournalEntry::class], version = 7, exportSchema = false)
abstract class GameDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao

    companion object {
        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getDatabase(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GameDatabase::class.java,
                    "light_and_darkness_database"
                )
                .addMigrations(MIGRATION_6_7)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `journal_entry_new` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `floor` INTEGER NOT NULL,
                        `actionKey` TEXT NOT NULL,
                        `actionArgsEncoded` TEXT NOT NULL,
                        `legacyText` TEXT NOT NULL,
                        `sideAlignmentShift` TEXT NOT NULL,
                        `alignmentImpact` INTEGER NOT NULL,
                        `timestamp` INTEGER NOT NULL,
                        `nodeIndex` INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL(
                    """
                    INSERT INTO `journal_entry_new` (
                        `id`,
                        `floor`,
                        `actionKey`,
                        `actionArgsEncoded`,
                        `legacyText`,
                        `sideAlignmentShift`,
                        `alignmentImpact`,
                        `timestamp`,
                        `nodeIndex`
                    )
                    SELECT
                        `id`,
                        `floor`,
                        '',
                        '',
                        COALESCE(`actionTakenTr`, `actionTakenEs`, ''),
                        `sideAlignmentShift`,
                        `alignmentImpact`,
                        `timestamp`,
                        `nodeIndex`
                    FROM `journal_entry`
                    """.trimIndent()
                )
                db.execSQL("DROP TABLE `journal_entry`")
                db.execSQL("ALTER TABLE `journal_entry_new` RENAME TO `journal_entry`")
            }
        }
    }
}
