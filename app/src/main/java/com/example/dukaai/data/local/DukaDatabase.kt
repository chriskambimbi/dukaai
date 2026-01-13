package com.example.dukaai.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.dukaai.data.local.dao.*
import com.example.dukaai.data.local.entity.*

/**
 * Duka.AI Room Database
 * Offline-first local database for all app data
 *
 * IMPORTANT: When modifying the schema:
 * 1. Increment the version number
 * 2. Add a migration in DatabaseMigrations.kt
 * 3. Add the migration to ALL_MIGRATIONS array
 * 4. Test the migration thoroughly before release
 */
@Database(
    entities = [
        ProductEntity::class,
        SaleEntity::class,
        CustomerEntity::class,
        CreditLedgerEntity::class,
        PaymentEntity::class,
        InventoryLogEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class DukaDatabase : RoomDatabase() {

    abstract fun productDao(): ProductDao
    abstract fun saleDao(): SaleDao
    abstract fun customerDao(): CustomerDao
    abstract fun creditLedgerDao(): CreditLedgerDao
    abstract fun paymentDao(): PaymentDao
    abstract fun inventoryLogDao(): InventoryLogDao

    companion object {
        @Volatile
        private var INSTANCE: DukaDatabase? = null

        private const val DATABASE_NAME = "duka_database"

        fun getInstance(context: Context): DukaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DukaDatabase::class.java,
                    DATABASE_NAME
                )
                    // Add all migrations for proper data preservation
                    .addMigrations(*DatabaseMigrations.ALL_MIGRATIONS)
                    // Only allow destructive migration on downgrade (e.g., installing older APK)
                    // This preserves data during normal upgrades
                    .fallbackToDestructiveMigrationOnDowngrade()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
