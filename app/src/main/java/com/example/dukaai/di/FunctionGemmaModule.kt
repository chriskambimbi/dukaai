package com.example.dukaai.di

import android.content.Context
import com.example.dukaai.data.local.dao.InventoryLogDao
import com.example.dukaai.data.repository.CreditRepository
import com.example.dukaai.data.repository.CustomerRepository
import com.example.dukaai.data.repository.PaymentRepository
import com.example.dukaai.data.repository.ProductRepository
import com.example.dukaai.data.repository.SaleRepository
import com.example.dukaai.ml.functiongemma.DukaFunctionExecutor
import com.example.dukaai.ml.functiongemma.FunctionGemmaInference
import com.example.dukaai.ml.functiongemma.FunctionGemmaParser
import com.example.dukaai.ml.functiongemma.FunctionGemmaService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for FunctionGemma components.
 *
 * Provides the FunctionGemma inference engine, parser, executor, and service
 * for natural language command processing in DukaAI.
 */
@Module
@InstallIn(SingletonComponent::class)
object FunctionGemmaModule {

    /**
     * Provides the FunctionGemma inference engine for TFLite model execution.
     */
    @Provides
    @Singleton
    fun provideFunctionGemmaInference(
        @ApplicationContext context: Context
    ): FunctionGemmaInference {
        return FunctionGemmaInference(context)
    }

    /**
     * Provides the FunctionGemma parser for extracting function calls from model output.
     */
    @Provides
    @Singleton
    fun provideFunctionGemmaParser(): FunctionGemmaParser {
        return FunctionGemmaParser()
    }

    /**
     * Provides the function executor that maps function calls to repository operations.
     */
    @Provides
    @Singleton
    fun provideDukaFunctionExecutor(
        productRepository: ProductRepository,
        saleRepository: SaleRepository,
        customerRepository: CustomerRepository,
        creditRepository: CreditRepository,
        paymentRepository: PaymentRepository,
        inventoryLogDao: InventoryLogDao
    ): DukaFunctionExecutor {
        return DukaFunctionExecutor(
            productRepository = productRepository,
            saleRepository = saleRepository,
            customerRepository = customerRepository,
            creditRepository = creditRepository,
            paymentRepository = paymentRepository,
            inventoryLogDao = inventoryLogDao
        )
    }

    /**
     * Provides the high-level FunctionGemma service that orchestrates
     * command processing from natural language to execution.
     */
    @Provides
    @Singleton
    fun provideFunctionGemmaService(
        inference: FunctionGemmaInference,
        parser: FunctionGemmaParser,
        executor: DukaFunctionExecutor
    ): FunctionGemmaService {
        return FunctionGemmaService(
            inference = inference,
            parser = parser,
            executor = executor
        )
    }
}
