package com.example.dukaai.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Sale entity for Room database
 */
@Entity(
    tableName = "sales",
    foreignKeys = [
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["id"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CustomerEntity::class,
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("productId"), Index("customerId")]
)
data class SaleEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val productId: String,
    val customerId: String? = null, // Null for cash sales
    val quantity: Int,
    val unitPrice: Double,
    val totalAmount: Double,
    val saleType: String, // "CASH" or "CREDIT"
    val timestamp: Long = System.currentTimeMillis()
)
