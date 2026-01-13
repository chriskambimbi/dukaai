package com.example.dukaai.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dukaai.ui.theme.*
import kotlin.math.abs

/**
 * Modern Customer Card for Credit Ledger
 *
 * Features:
 * - Initials avatar with pastel background
 * - Clean layout with subtle border
 * - WhatsApp and Pay quick action buttons
 * - Bold right-aligned debt amount
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
    modifier: Modifier = Modifier,
    onWhatsAppClick: (() -> Unit)? = null,
    onPayClick: (() -> Unit)? = null
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Customer initials avatar
            CustomerInitialsAvatar(name = customerName)

            Spacer(modifier = Modifier.width(12.dp))

            // Customer info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Name
                Text(
                    text = customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = SlateTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Payment status and unpaid count
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PaymentStatusChip(status = paymentStatus)
                    Text(
                        text = "$unpaidTransactions unpaid",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Quick action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // WhatsApp button (only if phone number exists)
                    if (phoneNumber != null && onWhatsAppClick != null) {
                        TextButton(
                            onClick = onWhatsAppClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = Color(0xFF25D366) // WhatsApp green
                            )
                        ) {
                            Text(
                                text = "WhatsApp",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Pay button
                    if (onPayClick != null) {
                        TextButton(
                            onClick = onPayClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            colors = ButtonDefaults.textButtonColors(
                                contentColor = EmeraldAccent
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Payment,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Pay",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Debt amount - right aligned, bold
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "K ${formatDebt(totalDebt)}",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = if (totalDebt > 0) ErrorRed else EmeraldAccent
                )

                Text(
                    text = if (totalDebt > 0) "Owed" else "Paid",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateTextTertiary
                )
            }
        }
    }
}

/**
 * Customer initials avatar with pastel background
 */
@Composable
fun CustomerInitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48
) {
    // Get initials (first letter of first and last name)
    val initials = name
        .split(" ")
        .take(2)
        .mapNotNull { it.firstOrNull()?.uppercaseChar() }
        .joinToString("")
        .ifEmpty { name.take(1).uppercase() }

    // Generate consistent pastel color based on name
    val colorIndex = remember(name) {
        abs(name.hashCode()) % AvatarPastelColors.size
    }
    val backgroundColor = AvatarPastelColors[colorIndex]

    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = SlatePrimaryDark
        )
    }
}

/**
 * Payment status chip
 */
@Composable
fun PaymentStatusChip(
    status: PaymentStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, textColor, label) = when (status) {
        PaymentStatus.ON_TIME -> Triple(SuccessBg, SuccessGreen, "On Time")
        PaymentStatus.DUE_SOON -> Triple(WarningBg, WarningYellow, "Due Soon")
        PaymentStatus.OVERDUE -> Triple(ErrorBg, ErrorRed, "Overdue")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Payment status badge (legacy - kept for compatibility)
 */
@Composable
fun PaymentStatusBadge(
    status: PaymentStatus,
    statusText: String,
    statusColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = statusColor.copy(alpha = 0.12f)
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

/**
 * Format debt amount for display
 */
private fun formatDebt(amount: Double): String {
    return if (amount == amount.toLong().toDouble()) {
        amount.toLong().toString()
    } else {
        String.format("%.2f", amount)
    }
}

/**
 * Compact customer card for smaller displays
 */
@Composable
fun CompactCustomerCard(
    customerName: String,
    totalDebt: Double,
    paymentStatus: PaymentStatus,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .border(
                width = 1.dp,
                color = SlateBorder,
                shape = RoundedCornerShape(12.dp)
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomerInitialsAvatar(name = customerName, size = 40)

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = customerName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = SlateTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "K ${formatDebt(totalDebt)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (totalDebt > 0) ErrorRed else EmeraldAccent
            )
        }
    }
}
