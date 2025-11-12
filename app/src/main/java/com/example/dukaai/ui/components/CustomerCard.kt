package com.example.dukaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.*

/**
 * Customer Card component for credit ledger
 */
@Composable
fun CustomerCard(
    customerName: String,
    totalDebt: Double,
    phoneNumber: String?,
    unpaidTransactions: Int,
    lastPurchaseDate: String,
    paymentStatus: PaymentStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (statusColor, statusText) = when (paymentStatus) {
        PaymentStatus.ON_TIME -> Pair(CreditPaid, "On Time")
        PaymentStatus.DUE_SOON -> Pair(CreditDueSoon, "Due Soon")
        PaymentStatus.OVERDUE -> Pair(CreditOverdue, "Overdue")
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer avatar
            Surface(
                modifier = Modifier.size(56.dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Customer info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (phoneNumber != null) {
                    Text(
                        text = phoneNumber,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Payment status badge
                    PaymentStatusBadge(
                        status = paymentStatus,
                        statusText = statusText,
                        statusColor = statusColor
                    )

                    Text(
                        text = "$unpaidTransactions unpaid",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Last purchase: $lastPurchaseDate",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Debt amount
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "K ${String.format("%.2f", totalDebt)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (totalDebt > 0) ErrorRed else SuccessGreen
                )

                Text(
                    text = if (totalDebt > 0) "Owed" else "Paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

/**
 * Payment status badge
 */
@Composable
fun PaymentStatusBadge(
    status: PaymentStatus,
    statusText: String,
    statusColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.small,
        color = statusColor.copy(alpha = 0.2f)
    ) {
        Text(
            text = statusText,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = statusColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Payment status enum
 */
enum class PaymentStatus {
    ON_TIME, DUE_SOON, OVERDUE
}
