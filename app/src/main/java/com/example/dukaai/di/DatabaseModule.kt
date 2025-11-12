package com.example.dukaai.di

import android.content.Context
import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.local.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing database-related dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    /**
     * Provides the Duka database instance
     */
    @Provides
    @Singleton
    fun provideDukaDatabase(
        @ApplicationContext context: Context
    ): DukaDatabase {
        return DukaDatabase.getInstance(context)
    }

    /**
     * Provides ProductDao
     */
    @Provides
    @Singleton
    fun provideProductDao(database: DukaDatabase): ProductDao {
        return database.productDao()
    }

    /**
     * Provides SaleDao
     */
    @Provides
    @Singleton
    fun provideSaleDao(database: DukaDatabase): SaleDao {
        return database.saleDao()
    }

    /**
     * Provides CustomerDao
     */
    @Provides
    @Singleton
    fun provideCustomerDao(database: DukaDatabase): CustomerDao {
        return database.customerDao()
    }

    /**
     * Provides CreditLedgerDao
     */
    @Provides
    @Singleton
    fun provideCreditLedgerDao(database: DukaDatabase): CreditLedgerDao {
        return database.creditLedgerDao()
    }

    /**
     * Provides PaymentDao
     */
    @Provides
    @Singleton
    fun providePaymentDao(database: DukaDatabase): PaymentDao {
        return database.paymentDao()
    }

    /**
     * Provides InventoryLogDao
     */
    @Provides
    @Singleton
    fun provideInventoryLogDao(database: DukaDatabase): InventoryLogDao {
        return database.inventoryLogDao()
    }
}
