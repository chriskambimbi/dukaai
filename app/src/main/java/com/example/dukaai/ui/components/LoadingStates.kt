package com.example.dukaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.*

/**
 * Shimmer effect for loading states
 */
@Composable
fun shimmerBrush(
    showShimmer: Boolean = true,
    targetValue: Float = 1000f
): Brush {
    return if (showShimmer) {
        val shimmerColors = listOf(
            SlateSurfaceVariant,
            SlateSurfaceVariant.copy(alpha = 0.5f),
            SlateSurfaceVariant
        )

        val transition = rememberInfiniteTransition(label = "shimmer")
        val translateAnimation by transition.animateFloat(
            initialValue = 0f,
            targetValue = targetValue,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = 1200,
                    easing = FastOutSlowInEasing
                ),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmer_translate"
        )

        Brush.linearGradient(
            colors = shimmerColors,
            start = Offset(translateAnimation - 200f, translateAnimation - 200f),
            end = Offset(translateAnimation, translateAnimation)
        )
    } else {
        Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }
}

/**
 * Basic shimmer box for custom shapes
 */
@Composable
fun ShimmerBox(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

/**
 * Skeleton for a single line of text
 */
@Composable
fun TextSkeleton(
    width: Dp = 100.dp,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    ShimmerBox(
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(4.dp)
    )
}

/**
 * Skeleton for dashboard metric card
 */
@Composable
fun MetricCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextSkeleton(width = 80.dp, height = 14.dp)
                ShimmerBox(
                    modifier = Modifier.size(24.dp),
                    shape = CircleShape
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            TextSkeleton(width = 120.dp, height = 32.dp)

            Spacer(modifier = Modifier.height(8.dp))

            TextSkeleton(width = 60.dp, height = 12.dp)
        }
    }
}

/**
 * Skeleton for product list item
 */
@Composable
fun ProductCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar skeleton
            ShimmerBox(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Text content
            Column(modifier = Modifier.weight(1f)) {
                TextSkeleton(width = 140.dp, height = 16.dp)
                Spacer(modifier = Modifier.height(6.dp))
                TextSkeleton(width = 80.dp, height = 12.dp)
                Spacer(modifier = Modifier.height(8.dp))
                TextSkeleton(width = 100.dp, height = 20.dp)
            }

            // Price skeleton
            Column(horizontalAlignment = Alignment.End) {
                TextSkeleton(width = 60.dp, height = 20.dp)
                Spacer(modifier = Modifier.height(4.dp))
                TextSkeleton(width = 40.dp, height = 12.dp)
            }
        }
    }
}

/**
 * Skeleton for sales summary card
 */
@Composable
fun SalesSummarySkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextSkeleton(width = 100.dp, height = 18.dp)
                ShimmerBox(
                    modifier = Modifier
                        .width(60.dp)
                        .height(24.dp),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextSkeleton(width = 150.dp, height = 40.dp)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                repeat(3) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        ShimmerBox(
                            modifier = Modifier.size(20.dp),
                            shape = CircleShape
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextSkeleton(width = 40.dp, height = 18.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        TextSkeleton(width = 60.dp, height = 12.dp)
                    }
                }
            }
        }
    }
}

/**
 * Skeleton for chart/graph area
 */
@Composable
fun ChartSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 200.dp
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TextSkeleton(width = 120.dp, height = 18.dp)
                TextSkeleton(width = 80.dp, height = 14.dp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height),
                shape = RoundedCornerShape(8.dp)
            )
        }
    }
}

/**
 * Skeleton for customer/credit card
 */
@Composable
fun CustomerCardSkeleton(
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = SlateSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Top row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShimmerBox(
                    modifier = Modifier.size(44.dp),
                    shape = CircleShape
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    TextSkeleton(width = 120.dp, height = 16.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    TextSkeleton(width = 80.dp, height = 12.dp)
                }

                TextSkeleton(width = 70.dp, height = 24.dp)
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Bottom row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                )
                ShimmerBox(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp),
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }
    }
}

/**
 * Full screen loading state with message
 */
@Composable
fun LoadingScreen(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(SlateBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                color = EmeraldAccent,
                strokeWidth = 3.dp
            )
            androidx.compose.material3.Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = SlateTextSecondary
            )
        }
    }
}

/**
 * Inline loading indicator
 */
@Composable
fun InlineLoading(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        androidx.compose.material3.CircularProgressIndicator(
            modifier = Modifier.size(16.dp),
            color = EmeraldAccent,
            strokeWidth = 2.dp
        )
        androidx.compose.material3.Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodySmall,
            color = SlateTextSecondary
        )
    }
}

/**
 * Pull-to-refresh loading indicator
 */
@Composable
fun RefreshingIndicator(
    isRefreshing: Boolean,
    modifier: Modifier = Modifier
) {
    if (isRefreshing) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .background(EmeraldAccent.copy(alpha = 0.1f))
                .padding(12.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = EmeraldAccent,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
            androidx.compose.material3.Text(
                text = "Refreshing...",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldAccent
            )
        }
    }
}
