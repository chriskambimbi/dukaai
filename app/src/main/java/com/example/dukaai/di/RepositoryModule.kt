package com.example.dukaai.di

import com.example.dukaai.data.local.DukaDatabase
import com.example.dukaai.data.local.dao.*
import com.example.dukaai.data.repository.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for providing repository dependencies
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    /**
     * Provides ProductRepository
     */
    @Provides
    @Singleton
    fun provideProductRepository(
        productDao: ProductDao,
        inventoryLogDao: InventoryLogDao
    ): ProductRepository {
        return ProductRepository(productDao, inventoryLogDao)
    }

    /**
     * Provides SaleRepository
     * Injects database for transaction support
     */
    @Provides
    @Singleton
    fun provideSaleRepository(
        database: DukaDatabase,
        saleDao: SaleDao,
        productDao: ProductDao,
        inventoryLogDao: InventoryLogDao
    ): SaleRepository {
        return SaleRepository(database, saleDao, productDao, inventoryLogDao)
    }

    /**
     * Provides CustomerRepository
     */
    @Provides
    @Singleton
    fun provideCustomerRepository(
        customerDao: CustomerDao,
        creditLedgerDao: CreditLedgerDao
    ): CustomerRepository {
        return CustomerRepository(customerDao, creditLedgerDao)
    }

    /**
     * Provides CreditRepository
     */
    @Provides
    @Singleton
    fun provideCreditRepository(
        creditLedgerDao: CreditLedgerDao,
        paymentDao: PaymentDao
    ): CreditRepository {
        return CreditRepository(creditLedgerDao, paymentDao)
    }

    /**
     * Provides PaymentRepository
     */
    @Provides
    @Singleton
    fun providePaymentRepository(
        paymentDao: PaymentDao
    ): PaymentRepository {
        return PaymentRepository(paymentDao)
    }
}
