package com.example.dukaai.ui.components

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.dukaai.ui.theme.*
import java.util.Locale

/**
 * Voice input button - compact version for search bars
 */
@Composable
fun VoiceSearchButton(
    onVoiceResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = EmeraldAccent
) {
    val context = LocalContext.current
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasPermission = isGranted
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        matches?.firstOrNull()?.let { spokenText ->
            onVoiceResult(spokenText)
        }
    }

    IconButton(
        onClick = {
            if (hasPermission) {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(
                        RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                    )
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "Search by voice...")
                }
                if (SpeechRecognizer.isRecognitionAvailable(context)) {
                    speechLauncher.launch(intent)
                }
            } else {
                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
            }
        },
        modifier = modifier
    ) {
        Icon(
            imageVector = if (hasPermission) Icons.Default.Mic else Icons.Default.MicOff,
            contentDescription = "Voice search",
            tint = tint
        )
    }
}

/**
 * Voice command chip - shows available voice commands
 */
@Composable
fun VoiceCommandChip(
    command: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = EmeraldAccent.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = null,
                tint = EmeraldAccent,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "\"$command\"",
                style = MaterialTheme.typography.bodySmall,
                color = EmeraldAccent,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

/**
 * Voice input dialog - full screen voice input with visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceInputDialog(
    isVisible: Boolean,
    onDismiss: () -> Unit,
    onResult: (String) -> Unit,
    title: String = "Listening...",
    hint: String = "Speak now",
    exampleCommands: List<String> = emptyList()
) {
    val context = LocalContext.current

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
        matches?.firstOrNull()?.let { spokenText ->
            onResult(spokenText)
        }
        onDismiss()
    }

    // Launch speech recognizer when dialog becomes visible
    LaunchedEffect(isVisible) {
        if (isVisible) {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, hint)
            }
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechLauncher.launch(intent)
            } else {
                onDismiss()
            }
        }
    }

    if (isVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            containerColor = SlateSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Animated microphone
                PulsingMicrophoneIcon()

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateTextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = hint,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SlateTextSecondary
                )

                if (exampleCommands.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(24.dp))

                    Text(
                        text = "Try saying:",
                        style = MaterialTheme.typography.labelMedium,
                        color = SlateTextTertiary
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    exampleCommands.forEach { command ->
                        Text(
                            text = "\"$command\"",
                            style = MaterialTheme.typography.bodyMedium,
                            color = EmeraldAccent,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = SlateTextSecondary)
                }
            }
        }
    }
}

@Composable
private fun PulsingMicrophoneIcon() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(contentAlignment = Alignment.Center) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(120.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(EmeraldAccent.copy(alpha = alpha * 0.3f))
        )

        // Middle ring
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(EmeraldAccent.copy(alpha = 0.2f))
        )

        // Inner circle with mic
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(EmeraldAccent),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = "Listening",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

/**
 * Parses voice commands for analytics
 * Returns a pair of (action, parameters)
 */
fun parseAnalyticsVoiceCommand(command: String): Pair<String, Map<String, String>>? {
    val lowerCommand = command.lowercase()

    return when {
        // Time-based queries
        lowerCommand.contains("yesterday") -> "show_period" to mapOf("period" to "yesterday")
        lowerCommand.contains("last week") -> "show_period" to mapOf("period" to "last_week")
        lowerCommand.contains("last month") -> "show_period" to mapOf("period" to "last_month")
        lowerCommand.contains("today") -> "show_period" to mapOf("period" to "today")
        lowerCommand.contains("this week") -> "show_period" to mapOf("period" to "this_week")
        lowerCommand.contains("this month") -> "show_period" to mapOf("period" to "this_month")

        // Metric queries
        lowerCommand.contains("profit") -> "show_metric" to mapOf("metric" to "profit")
        lowerCommand.contains("revenue") || lowerCommand.contains("sales") ->
            "show_metric" to mapOf("metric" to "revenue")
        lowerCommand.contains("top") && lowerCommand.contains("product") ->
            "show_metric" to mapOf("metric" to "top_products")
        lowerCommand.contains("best sell") -> "show_metric" to mapOf("metric" to "top_products")

        // Export
        lowerCommand.contains("export") || lowerCommand.contains("share") ->
            "export" to emptyMap()

        else -> null
    }
}

/**
 * Parses voice commands for product search
 */
fun parseProductVoiceCommand(command: String): String {
    val lowerCommand = command.lowercase()

    // Check for specific intents
    return when {
        lowerCommand.startsWith("search for ") -> lowerCommand.removePrefix("search for ")
        lowerCommand.startsWith("find ") -> lowerCommand.removePrefix("find ")
        lowerCommand.startsWith("show me ") -> lowerCommand.removePrefix("show me ")
        lowerCommand.startsWith("look for ") -> lowerCommand.removePrefix("look for ")
        else -> command
    }
}
