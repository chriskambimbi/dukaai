package com.example.dukaai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Payment entity for Room database
 * Tracks payments made towards credit
 */
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = CreditLedgerEntity::class,
            parentColumns = ["id"],
            childColumns = ["creditId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("creditId")]
)
data class PaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val creditId: String,
    val amount: Double,
    val paymentMethod: String, // "CASH", "MOBILE_MONEY", "BANK_TRANSFER"
    val notes: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)
