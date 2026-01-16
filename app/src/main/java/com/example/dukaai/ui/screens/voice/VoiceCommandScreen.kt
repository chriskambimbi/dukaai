package com.example.dukaai.ui.screens.voice

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.dukaai.ui.components.EnhancedVoiceInputButton
import com.example.dukaai.ui.components.WaveformVisualizer
import com.example.dukaai.ui.components.VoiceButtonSize
import com.example.dukaai.ui.theme.*
import com.example.dukaai.ui.viewmodel.VoiceCommandViewModel
import com.example.dukaai.voice.*

/**
 * Voice Command Screen
 * Full-screen interface for voice commands with visual feedback
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceCommandScreen(
    navController: NavController,
    viewModel: VoiceCommandViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val recognitionState by viewModel.recognitionState.collectAsStateWithLifecycle()
    val currentLanguage by viewModel.currentLanguage.collectAsStateWithLifecycle()
    val parsedCommand by viewModel.parsedCommand.collectAsStateWithLifecycle()
    val executionResult by viewModel.executionResult.collectAsStateWithLifecycle()
    val isProcessing by viewModel.isProcessing.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val error by viewModel.error.collectAsStateWithLifecycle()

    var showLanguageDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = SlateBackground
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Modern gradient header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                AccentOrange,
                                AccentOrange.copy(alpha = 0.9f),
                                AccentOrange.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    // Top row with back and actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { navController.navigateUp() },
                            colors = IconButtonDefaults.iconButtonColors(
                                contentColor = Color.White
                            )
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            // Language chip
                            Surface(
                                onClick = { showLanguageDialog = true },
                                shape = RoundedCornerShape(20.dp),
                                color = Color.White.copy(alpha = 0.2f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        Icons.Default.Language,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = Color.White
                                    )
                                    Text(
                                        text = currentLanguage.displayName,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = "Voice Sale",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Speak to record a sale quickly",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            // Main content
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Status and recognized text section
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Status indicator
                    Text(
                        text = getStatusText(recognitionState, isProcessing),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        color = when {
                            isListening -> AccentOrange
                            isProcessing -> CopperPrimary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Recognized text card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp)
                                .padding(20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            when {
                                recognitionState is VoiceRecognitionState.Processing -> {
                                    Text(
                                        text = "\"${(recognitionState as VoiceRecognitionState.Processing).partialText}\"",
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        color = AccentOrange
                                    )
                                }
                                recognitionState is VoiceRecognitionState.Success -> {
                                    Text(
                                        text = "\"${(recognitionState as VoiceRecognitionState.Success).text}\"",
                                        style = MaterialTheme.typography.titleLarge,
                                        textAlign = TextAlign.Center,
                                        color = SuccessGreen,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                parsedCommand != null -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Surface(
                                            shape = RoundedCornerShape(8.dp),
                                            color = SuccessGreen.copy(alpha = 0.1f)
                                        ) {
                                            Text(
                                                text = getCommandTypeDisplayName(parsedCommand!!.type),
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Bold,
                                                color = SuccessGreen
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = parsedCommand!!.originalText,
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                                else -> {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            Icons.Outlined.RecordVoiceOver,
                                            contentDescription = null,
                                            modifier = Modifier.size(32.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = getInstructions(currentLanguage),
                                            style = MaterialTheme.typography.bodyLarge,
                                            textAlign = TextAlign.Center,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Execution result
                    if (executionResult != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        ExecutionResultCard(executionResult!!)
                    }

                    // Error message
                    if (error != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = ErrorRed.copy(alpha = 0.1f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    contentDescription = null,
                                    tint = ErrorRed
                                )
                                Text(
                                    text = error!!,
                                    color = ErrorRed
                                )
                            }
                        }
                    }
                }

                // Waveform visualization
                if (isListening) {
                    WaveformVisualizer(
                        isActive = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        barColor = AccentOrange
                    )
                }

                // Enhanced microphone button
                EnhancedVoiceInputButton(
                    isListening = isListening,
                    isProcessing = isProcessing,
                    onStartListening = { viewModel.startListening() },
                    onStopListening = { viewModel.stopListening() },
                    size = VoiceButtonSize.Large
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick examples at bottom
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "TRY SAYING",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ExampleCommands(currentLanguage)
                }
            }
        }
    }

    // Language selection dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = currentLanguage,
            onLanguageSelected = { language ->
                viewModel.setLanguage(language)
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

/**
 * Microphone button with animation
 */
@Composable
fun MicrophoneButton(
    isListening: Boolean,
    isProcessing: Boolean,
    onStartListening: () -> Unit,
    onStopListening: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(200.dp)
    ) {
        // Outer ring when listening
        if (isListening) {
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .scale(scale)
                    .border(
                        width = 4.dp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
            )
        }

        // Main button
        FloatingActionButton(
            onClick = {
                if (isListening) {
                    onStopListening()
                } else {
                    onStartListening()
                }
            },
            modifier = Modifier.size(120.dp),
            containerColor = when {
                isListening -> MaterialTheme.colorScheme.error
                isProcessing -> MaterialTheme.colorScheme.secondary
                else -> MaterialTheme.colorScheme.primary
            }
        ) {
            if (isProcessing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(48.dp),
                    color = MaterialTheme.colorScheme.onSecondary
                )
            } else {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start listening",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

/**
 * Execution result card
 */
@Composable
fun ExecutionResultCard(result: VoiceCommandResult) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = when (result) {
                is VoiceCommandResult.Success -> MaterialTheme.colorScheme.primaryContainer
                is VoiceCommandResult.Failure -> MaterialTheme.colorScheme.errorContainer
                is VoiceCommandResult.NeedsConfirmation -> MaterialTheme.colorScheme.tertiaryContainer
            }
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (result) {
                    is VoiceCommandResult.Success -> Icons.Default.CheckCircle
                    is VoiceCommandResult.Failure -> Icons.Default.Error
                    is VoiceCommandResult.NeedsConfirmation -> Icons.AutoMirrored.Filled.HelpOutline
                },
                contentDescription = null,
                tint = when (result) {
                    is VoiceCommandResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                    is VoiceCommandResult.Failure -> MaterialTheme.colorScheme.onErrorContainer
                    is VoiceCommandResult.NeedsConfirmation -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
            Text(
                text = when (result) {
                    is VoiceCommandResult.Success -> result.message
                    is VoiceCommandResult.Failure -> "${result.error}: ${result.reason}"
                    is VoiceCommandResult.NeedsConfirmation -> result.prompt
                },
                color = when (result) {
                    is VoiceCommandResult.Success -> MaterialTheme.colorScheme.onPrimaryContainer
                    is VoiceCommandResult.Failure -> MaterialTheme.colorScheme.onErrorContainer
                    is VoiceCommandResult.NeedsConfirmation -> MaterialTheme.colorScheme.onTertiaryContainer
                }
            )
        }
    }
}

/**
 * Example commands
 */
@Composable
fun ExampleCommands(language: VoiceLanguage) {
    val examples = when (language) {
        VoiceLanguage.ENGLISH -> listOf(
            "\"Record sale of 2 Coca-Cola\"",
            "\"Check stock for bread\"",
            "\"Add new product\""
        )
        VoiceLanguage.NYANJA -> listOf(
            "\"Gulitsa ziwiri Coca-Cola\"",
            "\"Onani katundu wa buledi\"",
            "\"Onjezani katundu katsopano\""
        )
        VoiceLanguage.BEMBA -> listOf(
            "\"Sula fibili Coca-Cola\"",
            "\"Monako ifintu fya buledi\"",
            "\"Onjezeko ifintu ifipya\""
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        examples.forEach { example ->
            Text(
                text = example,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Language selection dialog
 */
@Composable
fun LanguageSelectionDialog(
    currentLanguage: VoiceLanguage,
    onLanguageSelected: (VoiceLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Language") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                VoiceLanguage.values().forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onLanguageSelected(language) }
                        )
                        Text(
                            text = language.displayName,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

// Helper functions

fun getStatusText(state: VoiceRecognitionState, isProcessing: Boolean): String {
    return when {
        isProcessing -> "Processing..."
        state is VoiceRecognitionState.Listening -> "Listening..."
        state is VoiceRecognitionState.Processing -> "Recognizing..."
        state is VoiceRecognitionState.Success -> "Command recognized!"
        state is VoiceRecognitionState.Error -> "Error"
        else -> "Tap microphone to speak"
    }
}

fun getInstructions(language: VoiceLanguage): String {
    return when (language) {
        VoiceLanguage.ENGLISH -> "Tap the microphone and say a command"
        VoiceLanguage.NYANJA -> "Dinani maikolofoni ndilankhulani"
        VoiceLanguage.BEMBA -> "Kankamuna maikolofoni uwilande"
    }
}

fun getCommandTypeDisplayName(type: VoiceCommandType): String {
    return when (type) {
        VoiceCommandType.RECORD_SALE -> "Record Sale"
        VoiceCommandType.ADD_PRODUCT -> "Add Product"
        VoiceCommandType.CHECK_STOCK -> "Check Stock"
        VoiceCommandType.RECORD_PAYMENT -> "Record Payment"
        VoiceCommandType.ADD_CUSTOMER -> "Add Customer"
        VoiceCommandType.VIEW_ANALYTICS -> "View Analytics"
        VoiceCommandType.LOW_STOCK_ALERT -> "Low Stock Alert"
        VoiceCommandType.SEARCH -> "Search"
        VoiceCommandType.NAVIGATE -> "Navigate"
        VoiceCommandType.UNKNOWN -> "Unknown Command"
    }
}
