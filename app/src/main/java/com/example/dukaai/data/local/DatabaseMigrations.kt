package com.example.dukaai.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Database migrations for Duka.AI
 *
 * Each migration defines how to transform the database schema from one version to the next.
 * This preserves user data during app updates.
 *
 * Migration naming convention: MIGRATION_X_Y (from version X to version Y)
 */
object DatabaseMigrations {

    /**
     * All migrations in order.
     * Add new migrations to this array when schema changes.
     */
    val ALL_MIGRATIONS: Array<Migration> = arrayOf(
        // Add migrations here as the schema evolves
        // MIGRATION_1_2,
        // MIGRATION_2_3,
    )

    /**
     * Example migration from version 1 to 2.
     * Uncomment and modify when needed.
     *
     * Example: Adding a new column to products table
     */
    /*
    val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Add new column with default value
            db.execSQL("ALTER TABLE products ADD COLUMN supplier TEXT DEFAULT NULL")
        }
    }
    */

    /**
     * Example migration that adds a new table.
     */
    /*
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("""
                CREATE TABLE IF NOT EXISTS suppliers (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    phoneNumber TEXT,
                    email TEXT,
                    address TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)
        }
    }
    */

    /**
     * Example migration that modifies column type (requires table recreation).
     * SQLite doesn't support ALTER COLUMN, so we need to:
     * 1. Create new table with correct schema
     * 2. Copy data from old table
     * 3. Drop old table
     * 4. Rename new table
     */
    /*
    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            // Create new table with updated schema
            db.execSQL("""
                CREATE TABLE products_new (
                    id TEXT PRIMARY KEY NOT NULL,
                    name TEXT NOT NULL,
                    category TEXT NOT NULL,
                    currentStock INTEGER NOT NULL DEFAULT 0,
                    minStockThreshold INTEGER NOT NULL DEFAULT 10,
                    buyingPrice REAL NOT NULL DEFAULT 0.0,
                    sellingPrice REAL NOT NULL DEFAULT 0.0,
                    barcode TEXT,
                    imageUrl TEXT,
                    supplier TEXT,
                    createdAt INTEGER NOT NULL,
                    updatedAt INTEGER NOT NULL
                )
            """)

            // Copy data from old table
            db.execSQL("""
                INSERT INTO products_new (id, name, category, currentStock, minStockThreshold,
                    buyingPrice, sellingPrice, barcode, imageUrl, createdAt, updatedAt)
                SELECT id, name, category, currentStock, minStockThreshold,
                    buyingPrice, sellingPrice, barcode, imageUrl, createdAt, updatedAt
                FROM products
            """)

            // Drop old table
            db.execSQL("DROP TABLE products")

            // Rename new table to original name
            db.execSQL("ALTER TABLE products_new RENAME TO products")
        }
    }
    */
}
