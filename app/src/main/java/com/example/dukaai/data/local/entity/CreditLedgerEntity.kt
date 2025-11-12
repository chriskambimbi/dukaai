package com.example.dukaai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Credit Ledger entity for Room database
 * Tracks credit transactions
 */
@Entity(
    tableName = "credit_ledger",
    foreignKeys = [
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = SaleEntity::class,
            parentColumns = ["id"],
            childColumns = ["saleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("customerId"), Index("saleId")]
)
data class CreditLedgerEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val customerId: String,
    val saleId: String,
    val amount: Double,
    val amountPaid: Double = 0.0,
    val amountRemaining: Double = amount,
    val dueDate: Long? = null,
    val status: String, // "PENDING", "PARTIAL", "PAID", "OVERDUE"
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
