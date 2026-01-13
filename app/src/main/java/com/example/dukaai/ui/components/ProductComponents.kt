package com.example.dukaai.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.dukaai.data.local.entity.ProductEntity
import com.example.dukaai.ui.theme.SuccessGreen
import com.example.dukaai.ui.theme.WarningYellow

/**
 * Data class for activity items
 */
data class ActivityItem(
    val title: String,
    val description: String,
    val time: String,
    val icon: ImageVector
)

/**
 * Product header card with name, category, and stock badge
 */
@Composable
fun ProductHeaderCard(
    product: ProductEntity,
    stockStatus: StockStatus,
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text(product.category) }
                )

                StockBadge(
                    stockStatus = stockStatus,
                    currentStock = product.currentStock
                )
            }

            if (!product.barcode.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Barcode: ${product.barcode}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

/**
 * Stock information card
 */
@Composable
fun StockInfoCard(
    product: ProductEntity,
    modifier: Modifier = Modifier
) {
    val stockValue = product.currentStock * product.buyingPrice

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "STOCK INFORMATION",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                LabeledValue(label = "Current Stock", value = "${product.currentStock} units")
                LabeledValue(label = "Min. Threshold", value = "${product.minStockThreshold} units")
            }

            Spacer(modifier = Modifier.height(12.dp))

            LabeledValue(
                label = "Stock Value",
                value = "K ${String.format("%.2f", stockValue)}"
            )
        }
    }
}

/**
 * Price information card
 */
@Composable
fun PriceInfoCard(
    product: ProductEntity,
    profitMargin: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "PRICING",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                PriceColumn(
                    label = "Selling Price",
                    price = product.sellingPrice
                )

                PriceColumn(
                    label = "Buying Price",
                    price = product.buyingPrice,
                    alignment = Alignment.End
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            ProfitMarginRow(
                profit = product.sellingPrice - product.buyingPrice,
                marginPercent = profitMargin
            )
        }
    }
}

@Composable
private fun PriceColumn(
    label: String,
    price: Double,
    alignment: Alignment.Horizontal = Alignment.Start
) {
    Column(horizontalAlignment = alignment) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = "K ${String.format("%.2f", price)}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun ProfitMarginRow(
    profit: Double,
    marginPercent: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Profit per unit: K ${String.format("%.2f", profit)}",
            style = MaterialTheme.typography.bodyMedium
        )

        MarginBadge(marginPercent = marginPercent)
    }
}

@Composable
private fun MarginBadge(marginPercent: Int) {
    val isGoodMargin = marginPercent >= 25
    Surface(
        shape = MaterialTheme.shapes.small,
        color = if (isGoodMargin) SuccessGreen.copy(alpha = 0.2f) else WarningYellow.copy(alpha = 0.2f)
    ) {
        Text(
            text = "$marginPercent% margin",
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = if (isGoodMargin) SuccessGreen else WarningYellow,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Labeled value display
 */
@Composable
fun LabeledValue(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Activity item card
 */
@Composable
fun ActivityItemCard(
    activity: ActivityItem,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = activity.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }

            Text(
                text = activity.time,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}

/**
 * Dialog for restocking a product
 */
@Composable
fun RestockDialog(
    productName: String,
    currentStock: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var quantity by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restock $productName") },
        text = {
            Column {
                Text("Current stock: $currentStock units")
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = quantity,
                    onValueChange = { quantity = it },
                    label = { Text("Quantity to add") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { quantity.toIntOrNull()?.let { onConfirm(it) } },
                enabled = quantity.toIntOrNull()?.let { it > 0 } ?: false
            ) {
                Text("Restock")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dialog for editing product details
 */
@Composable
fun EditProductDialog(
    product: ProductEntity,
    onDismiss: () -> Unit,
    onConfirm: (ProductEntity) -> Unit
) {
    var name by remember { mutableStateOf(product.name) }
    var sellingPrice by remember { mutableStateOf(product.sellingPrice.toString()) }
    var buyingPrice by remember { mutableStateOf(product.buyingPrice.toString()) }
    var minThreshold by remember { mutableStateOf(product.minStockThreshold.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Product") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Product Name") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = sellingPrice,
                    onValueChange = { sellingPrice = it },
                    label = { Text("Selling Price (K)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = buyingPrice,
                    onValueChange = { buyingPrice = it },
                    label = { Text("Buying Price (K)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
                OutlinedTextField(
                    value = minThreshold,
                    onValueChange = { minThreshold = it },
                    label = { Text("Min. Stock Threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedProduct = product.copy(
                    name = name,
                    sellingPrice = sellingPrice.toDoubleOrNull() ?: product.sellingPrice,
                    buyingPrice = buyingPrice.toDoubleOrNull() ?: product.buyingPrice,
                    minStockThreshold = minThreshold.toIntOrNull() ?: product.minStockThreshold,
                    updatedAt = System.currentTimeMillis()
                )
                onConfirm(updatedProduct)
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Coming soon placeholder card
 */
@Composable
fun ComingSoonCard(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
