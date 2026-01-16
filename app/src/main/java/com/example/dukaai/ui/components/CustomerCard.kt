package com.example.dukaai.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.outlined.Payment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dukaai.ui.theme.*
import kotlin.math.abs

// WhatsApp brand green
private val WhatsAppGreen = Color(0xFF25D366)

/**
 * Modern Customer Card for Credit Ledger
 *
 * Two-row layout:
 * - Top row: Avatar + Name + Amount
 * - Bottom row: Status + Actions (Send Reminder, Pay, History)
 *
 * Features:
 * - Red border for overdue customers
 * - Prominent "Send Reminder" button for WhatsApp
 * - View History option
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
    onPayClick: (() -> Unit)? = null,
    onViewHistoryClick: (() -> Unit)? = null
) {
    val isOverdue = paymentStatus == PaymentStatus.OVERDUE
    val cardBorder = if (isOverdue) {
        BorderStroke(2.dp, ErrorRed)
    } else {
        BorderStroke(1.dp, SlateBorder)
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Top Row: Avatar + Name + Amount
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Customer initials avatar
                CustomerInitialsAvatar(
                    name = customerName,
                    isOverdue = isOverdue
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name and last purchase
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = customerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SlateTextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Last purchase: $lastPurchaseDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )
                }

                // Amount - right aligned
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "K ${formatDebt(totalDebt)}",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        ),
                        color = if (isOverdue) ErrorRed else if (totalDebt > 0) SlateTextPrimary else EmeraldAccent
                    )

                    Text(
                        text = "$unpaidTransactions unpaid",
                        style = MaterialTheme.typography.bodySmall,
                        color = SlateTextTertiary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Divider
            HorizontalDivider(color = SlateBorder.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom Row: Status + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Payment status chip
                PaymentStatusChip(status = paymentStatus)

                // Action buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // View History button
                    if (onViewHistoryClick != null) {
                        TextButton(
                            onClick = onViewHistoryClick,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.History,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                                tint = SlateTextSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.labelMedium,
                                color = SlateTextSecondary
                            )
                        }
                    }

                    // Send Reminder button (WhatsApp)
                    if (phoneNumber != null && onWhatsAppClick != null) {
                        FilledTonalButton(
                            onClick = onWhatsAppClick,
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = WhatsAppGreen.copy(alpha = 0.15f),
                                contentColor = WhatsAppGreen
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send Reminder",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Pay button
                    if (onPayClick != null) {
                        Button(
                            onClick = onPayClick,
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = EmeraldAccent,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp)
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
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Customer initials avatar with pastel background
 * Shows red ring for overdue customers
 */
@Composable
fun CustomerInitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Int = 48,
    isOverdue: Boolean = false
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
            .then(
                if (isOverdue) {
                    Modifier.border(2.dp, ErrorRed, CircleShape)
                } else {
                    Modifier
                }
            )
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
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold
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
    val isOverdue = paymentStatus == PaymentStatus.OVERDUE

    Card(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = SlateSurface
        ),
        border = if (isOverdue) BorderStroke(2.dp, ErrorRed) else BorderStroke(1.dp, SlateBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CustomerInitialsAvatar(name = customerName, size = 40, isOverdue = isOverdue)

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
                color = if (isOverdue) ErrorRed else if (totalDebt > 0) SlateTextPrimary else EmeraldAccent
            )
        }
    }
}
