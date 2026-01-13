package com.example.dukaai.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.AccentOrange
import com.example.dukaai.ui.theme.CopperPrimary
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dukaai.ui.viewmodel.VoiceCommandViewModel
import com.example.dukaai.voice.VoiceCommandResult
import com.example.dukaai.voice.VoiceRecognitionState

/**
 * Reusable voice input button component
 * Can be added to any screen for quick voice command access
 */
@Composable
fun VoiceInputButton(
    onCommandResult: ((VoiceCommandResult) -> Unit)? = null,
    viewModel: VoiceCommandViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val executionResult by viewModel.executionResult.collectAsStateWithLifecycle()
    val recognitionState by viewModel.recognitionState.collectAsStateWithLifecycle()

    var showResultDialog by remember { mutableStateOf(false) }

    // Handle execution results
    LaunchedEffect(executionResult) {
        if (executionResult != null) {
            showResultDialog = true
            onCommandResult?.invoke(executionResult!!)
        }
    }

    Box(modifier = modifier) {
        VoiceInputFAB(
            isListening = isListening,
            isProcessing = isProcessing,
            onStartListening = { viewModel.startListening() },
            onStopListening = { viewModel.stopListening() }
        )

        // Show result dialog
        if (showResultDialog && executionResult != null) {
            VoiceResultDialog(
                result = executionResult!!,
                onDismiss = {
                    showResultDialog = false
                    viewModel.clearExecutionResult()
                }
            )
        }
    }
}

/**
 * Voice input floating action button
 */
@Composable
fun VoiceInputFAB(
    isListening: Boolean,
    isProcessing: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "voice_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
    ) {
        // Pulsing ring when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
                    .border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }

        // Main FAB
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            containerColor = when {
                isListening -> MaterialTheme.colorScheme.error
                isProcessing -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(56.dp)
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onSecondary,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop" else "Voice",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Voice result dialog
 */
@Composable
fun VoiceResultDialog(
    result: VoiceCommandResult,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = when (result) {
                    is VoiceCommandResult.Success -> Icons.Default.Mic
                    is VoiceCommandResult.Failure -> Icons.Default.Mic
                    is VoiceCommandResult.NeedsConfirmation -> Icons.Default.Mic
                },
                contentDescription = null,
                tint = when (result) {
                    is VoiceCommandResult.Success -> MaterialTheme.colorScheme.primary
                    is VoiceCommandResult.Failure -> MaterialTheme.colorScheme.error
                    is VoiceCommandResult.NeedsConfirmation -> MaterialTheme.colorScheme.tertiary
                }
            )
        },
        title = {
            Text(
                text = when (result) {
                    is VoiceCommandResult.Success -> "Success"
                    is VoiceCommandResult.Failure -> "Error"
                    is VoiceCommandResult.NeedsConfirmation -> "Confirmation Needed"
                }
            )
        },
        text = {
            Text(
                text = when (result) {
                    is VoiceCommandResult.Success -> result.message
                    is VoiceCommandResult.Failure -> "${result.error}: ${result.reason}"
                    is VoiceCommandResult.NeedsConfirmation -> result.prompt
                }
            )
        },
        confirmButton = {
            when (result) {
                is VoiceCommandResult.NeedsConfirmation -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel")
                        }
                        Button(onClick = { /* Handle confirmation */ onDismiss() }) {
                            Text("Confirm")
                        }
                    }
                }
                else -> {
                    TextButton(onClick = onDismiss) {
                        Text("OK")
                    }
                }
            }
        }
    )
}

/**
 * Compact voice input indicator (for screens with limited space)
 */
@Composable
fun CompactVoiceIndicator(
    isListening: Boolean,
    isProcessing: Boolean,
    recognizedText: String?,
    modifier: Modifier = Modifier
) {
    if (isListening || isProcessing || recognizedText != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isListening -> MaterialTheme.colorScheme.errorContainer
                    isProcessing -> MaterialTheme.colorScheme.secondaryContainer
                    else -> MaterialTheme.colorScheme.primaryContainer
                }
            )
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = when {
                        isListening -> "Listening..."
                        isProcessing -> "Processing..."
                        recognizedText != null -> "\"$recognizedText\""
                        else -> ""
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Beautiful waveform visualization component
 * Displays animated sound wave bars when actively listening
 */
@Composable
fun WaveformVisualizer(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    barCount: Int = 40,
    barColor: Color = AccentOrange
) {
    var waveHeights by remember { mutableStateOf(List(barCount) { 0.15f }) }

    LaunchedEffect(isActive) {
        while (isActive) {
            waveHeights = List(barCount) { index ->
                val baseHeight = 0.3f
                val waveEffect = sin((index * 0.3f) + (System.currentTimeMillis() / 100f)) * 0.3f
                val randomness = Random.nextFloat() * 0.4f
                (baseHeight + waveEffect + randomness).coerceIn(0.1f, 1f)
            }
            delay(50)
        }
        // Reset to idle state
        waveHeights = List(barCount) { 0.15f }
    }

    Canvas(modifier = modifier.fillMaxWidth().height(60.dp)) {
        val barWidth = size.width / barCount
        val spacing = barWidth * 0.3f
        val effectiveBarWidth = barWidth - spacing

        waveHeights.forEachIndexed { index, height ->
            val barHeight = size.height * height
            val x = index * barWidth + spacing / 2
            val y = (size.height - barHeight) / 2

            drawRoundRect(
                color = if (isActive) barColor else barColor.copy(alpha = 0.3f),
                topLeft = Offset(x, y),
                size = Size(effectiveBarWidth, barHeight),
                cornerRadius = CornerRadius(effectiveBarWidth / 2, effectiveBarWidth / 2)
            )
        }
    }
}

/**
 * Circular waveform ripple effect that wraps around a button
 */
@Composable
fun CircularWaveform(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    waveColor: Color = AccentOrange
) {
    val infiniteTransition = rememberInfiniteTransition(label = "circular_wave")

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, delayMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    if (isActive) {
        Box(modifier = modifier) {
            listOf(wave1, wave2, wave3).forEach { progress ->
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .scale(1f + progress * 0.5f)
                        .border(
                            width = (2 * (1 - progress)).dp,
                            color = waveColor.copy(alpha = 0.5f * (1 - progress)),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

/**
 * Enhanced voice input FAB with waveform background
 */
@Composable
fun EnhancedVoiceInputButton(
    isListening: Boolean,
    isProcessing: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier,
    size: VoiceButtonSize = VoiceButtonSize.Large
) {
    val buttonSize = when (size) {
        VoiceButtonSize.Small -> 64.dp
        VoiceButtonSize.Medium -> 80.dp
        VoiceButtonSize.Large -> 120.dp
    }

    val iconSize = when (size) {
        VoiceButtonSize.Small -> 28.dp
        VoiceButtonSize.Medium -> 36.dp
        VoiceButtonSize.Large -> 48.dp
    }

    val infiniteTransition = rememberInfiniteTransition(label = "voice_button")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(buttonSize + 48.dp)
    ) {
        // Circular waveform when listening
        if (isListening) {
            CircularWaveform(
                isActive = true,
                modifier = Modifier.size(buttonSize + 40.dp),
                waveColor = AccentOrange
            )
        }

        // Outer glow rings
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(buttonSize + 20.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(AccentOrange.copy(alpha = 0.15f))
            )
            Box(
                modifier = Modifier
                    .size(buttonSize + 10.dp)
                    .scale(pulseScale * 0.97f)
                    .clip(CircleShape)
                    .background(AccentOrange.copy(alpha = 0.25f))
            )
        }

        // Main button
        FloatingActionButton(
            onClick = {
                if (isListening) onStopListening() else onStartListening()
            },
            modifier = Modifier.size(buttonSize),
            containerColor = when {
                isListening -> AccentOrange
                isProcessing -> CopperPrimary
                else -> MaterialTheme.colorScheme.primary
            },
            shape = CircleShape
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(iconSize * 0.7f),
                    color = Color.White,
                    strokeWidth = 3.dp
                )
            } else {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start voice input",
                    modifier = Modifier.size(iconSize),
                    tint = Color.White
                )
            }
        }
    }
}

enum class VoiceButtonSize {
    Small, Medium, Large
}

/**
 * Full voice input section with waveform, status, and transcription
 */
@Composable
fun VoiceInputSection(
    isListening: Boolean,
    isProcessing: Boolean,
    transcribedText: String?,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Status text
        Text(
            text = when {
                isProcessing -> "Processing..."
                isListening -> "Listening..."
                transcribedText != null -> "Recognized"
                else -> "Tap to speak"
            },
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Transcribed text display
        if (transcribedText != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Text(
                    text = "\"$transcribedText\"",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        // Waveform visualizer
        if (isListening) {
            WaveformVisualizer(
                isActive = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Enhanced voice button
        EnhancedVoiceInputButton(
            isListening = isListening,
            isProcessing = isProcessing,
            onStartListening = onStartListening,
            onStopListening = onStopListening,
            size = VoiceButtonSize.Large
        )
    }
}

private val EaseInOutCubic = CubicBezierEasing(0.65f, 0f, 0.35f, 1f)
