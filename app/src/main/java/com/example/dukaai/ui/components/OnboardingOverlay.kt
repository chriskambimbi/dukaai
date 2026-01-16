package com.example.dukaai.ui.components

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.dukaai.ui.theme.*

private const val PREFS_NAME = "duka_onboarding"
private const val KEY_ONBOARDING_COMPLETE = "onboarding_complete"
private const val KEY_CURRENT_STEP = "current_step"

/**
 * Onboarding step data
 */
data class OnboardingStep(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val description: String,
    val actionLabel: String? = null
)

/**
 * Default onboarding steps for DukaAI
 */
val defaultOnboardingSteps = listOf(
    OnboardingStep(
        id = "welcome",
        icon = Icons.Default.Store,
        title = "Welcome to Duka.AI",
        description = "Your smart shop assistant. Track sales, manage inventory, and grow your business with AI-powered insights."
    ),
    OnboardingStep(
        id = "quick_sale",
        icon = Icons.Default.ShoppingCart,
        title = "Make Sales Quickly",
        description = "Tap 'Sell' in the bottom bar to record sales. Use voice, scan barcodes, or search - whichever is fastest for you.",
        actionLabel = "Try it now"
    ),
    OnboardingStep(
        id = "voice",
        icon = Icons.Default.Mic,
        title = "Voice-First Design",
        description = "Just speak! Say 'two Coca-Cola' or 'search for bread'. Works even while your hands are busy.",
        actionLabel = "Enable voice"
    ),
    OnboardingStep(
        id = "offline",
        icon = Icons.Default.CloudOff,
        title = "Works Offline",
        description = "No internet? No problem. Make sales offline and everything syncs automatically when you're back online."
    ),
    OnboardingStep(
        id = "analytics",
        icon = Icons.Default.Analytics,
        title = "Track Your Growth",
        description = "View sales trends, top products, and AI insights to make smarter business decisions.",
        actionLabel = "View analytics"
    )
)

/**
 * Onboarding state manager
 */
@Composable
fun rememberOnboardingState(
    steps: List<OnboardingStep> = defaultOnboardingSteps
): OnboardingState {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE) }

    return remember {
        OnboardingState(
            steps = steps,
            isComplete = prefs.getBoolean(KEY_ONBOARDING_COMPLETE, false),
            currentStepIndex = prefs.getInt(KEY_CURRENT_STEP, 0),
            onComplete = {
                prefs.edit()
                    .putBoolean(KEY_ONBOARDING_COMPLETE, true)
                    .apply()
            },
            onStepChange = { index ->
                prefs.edit()
                    .putInt(KEY_CURRENT_STEP, index)
                    .apply()
            }
        )
    }
}

class OnboardingState(
    val steps: List<OnboardingStep>,
    isComplete: Boolean,
    currentStepIndex: Int,
    private val onComplete: () -> Unit,
    private val onStepChange: (Int) -> Unit
) {
    var isOnboardingComplete by mutableStateOf(isComplete)
        private set

    var currentStep by mutableIntStateOf(currentStepIndex.coerceIn(0, steps.lastIndex))
        private set

    val currentStepData: OnboardingStep
        get() = steps[currentStep]

    val isLastStep: Boolean
        get() = currentStep == steps.lastIndex

    val progress: Float
        get() = (currentStep + 1).toFloat() / steps.size

    fun nextStep() {
        if (isLastStep) {
            complete()
        } else {
            currentStep++
            onStepChange(currentStep)
        }
    }

    fun previousStep() {
        if (currentStep > 0) {
            currentStep--
            onStepChange(currentStep)
        }
    }

    fun skipToStep(index: Int) {
        currentStep = index.coerceIn(0, steps.lastIndex)
        onStepChange(currentStep)
    }

    fun complete() {
        isOnboardingComplete = true
        onComplete()
    }

    fun reset() {
        isOnboardingComplete = false
        currentStep = 0
        onStepChange(0)
    }
}

/**
 * Full-screen onboarding overlay
 */
@Composable
fun OnboardingOverlay(
    state: OnboardingState,
    onActionClick: ((String) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = !state.isOnboardingComplete,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.85f))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { /* Consume clicks */ }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Step content
                AnimatedContent(
                    targetState = state.currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }
                    },
                    label = "step_content"
                ) { stepIndex ->
                    OnboardingStepContent(
                        step = state.steps[stepIndex],
                        onActionClick = onActionClick
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Progress indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 32.dp)
                ) {
                    state.steps.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == state.currentStep) 24.dp else 8.dp, 8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (index == state.currentStep)
                                        EmeraldAccent
                                    else if (index < state.currentStep)
                                        EmeraldAccent.copy(alpha = 0.5f)
                                    else
                                        Color.White.copy(alpha = 0.3f)
                                )
                                .clickable { state.skipToStep(index) }
                        )
                    }
                }

                // Navigation buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Skip button
                    TextButton(
                        onClick = { state.complete() }
                    ) {
                        Text(
                            text = "Skip",
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Next/Done button
                    Button(
                        onClick = { state.nextStep() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldAccent
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (state.isLastStep) "Get Started" else "Next",
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!state.isLastStep) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OnboardingStepContent(
    step: OnboardingStep,
    onActionClick: ((String) -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(horizontal = 16.dp)
    ) {
        // Icon with animated ring
        Box(contentAlignment = Alignment.Center) {
            // Outer ring
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(EmeraldAccent.copy(alpha = 0.1f))
            )

            // Inner circle
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(EmeraldAccent.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = step.icon,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(48.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = step.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Description
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge,
            color = Color.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.4
        )

        // Optional action button
        if (step.actionLabel != null && onActionClick != null) {
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { onActionClick(step.id) },
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = EmeraldAccent
                ),
                border = ButtonDefaults.outlinedButtonBorder.copy(
                    brush = androidx.compose.ui.graphics.SolidColor(EmeraldAccent)
                )
            ) {
                Text(step.actionLabel)
            }
        }
    }
}

/**
 * Tooltip-style hint for specific UI elements
 */
@Composable
fun FeatureHint(
    text: String,
    isVisible: Boolean,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn() + scaleIn(initialScale = 0.8f),
        exit = fadeOut() + scaleOut(targetScale = 0.8f),
        modifier = modifier
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = SlatePrimaryDark,
            shadowElevation = 8.dp,
            onClick = onDismiss
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = EmeraldAccent,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White
                )
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Pulsing highlight for drawing attention to UI elements
 */
@Composable
fun PulsingHighlight(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = modifier
            .size(60.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .scale(scale)
                .clip(CircleShape)
                .background(EmeraldAccent.copy(alpha = alpha))
        )
    }
}
