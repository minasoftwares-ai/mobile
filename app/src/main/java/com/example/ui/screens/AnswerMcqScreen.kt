package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionInfo
import com.example.model.QuestionType
import com.example.model.ScreenState
import com.example.ui.components.CircularCountdownTimer
import com.example.ui.components.ReconnectingBanner
import com.example.ui.components.TopPlayerHeader
import com.example.ui.theme.ColorChoiceFalse
import com.example.ui.theme.ColorChoiceTrue
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusConnectedBg
import com.example.ui.theme.StatusConnectedBorder
import com.example.ui.theme.StatusConnectedText
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmDarkBrown
import com.example.ui.theme.WarmMediumBrown
import com.example.ui.theme.WarmOrangePrimary
import com.example.ui.theme.WarmTrack

@Composable
fun AnswerMcqScreen(
    state: ScreenState.ActiveQuestion,
    connectionInfo: ConnectionInfo,
    playerName: String = "",
    onAnswerSelected: (String) -> Unit,
    onSimulateNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isAnswerSubmitted = state.submittedAnswer != null
    val canTap = state.allowAnswer && !isAnswerSubmitted && state.remainingSeconds > 0

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        // Reconnecting Banner
        ReconnectingBanner(isReconnecting = connectionInfo.isReconnecting)

        // Top Player Header Bar (Warm Organic style)
        TopPlayerHeader(
            playerName = playerName,
            connectionInfo = connectionInfo
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Timer, Question pill & Helper Text
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Countdown Timer
                CircularCountdownTimer(
                    totalSeconds = state.totalSeconds,
                    remainingSeconds = state.remainingSeconds,
                    size = 96.dp
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Question Pill
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(WarmOrangePrimary)
                        .padding(horizontal = 18.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (state.totalQuestions > 0)
                            "السؤال ${state.questionNumber} من ${state.totalQuestions}"
                        else
                            "السؤال رقم ${state.questionNumber}",
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 14.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Subtitle Instruction
                Text(
                    text = "نص السؤال معروض على شاشة العرض الرئيسية",
                    color = WarmMediumBrown,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                )

                // Submission Status Banner
                if (isAnswerSubmitted) {
                    Spacer(modifier = Modifier.height(8.dp))
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + scaleIn()
                    ) {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(StatusConnectedBg)
                                .border(1.dp, StatusConnectedBorder, RoundedCornerShape(50))
                                .padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = StatusConnectedText,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تم إرسال إجابتك: (${state.submittedAnswer})",
                                color = StatusConnectedText,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                } else if (state.remainingSeconds <= 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(ColorChoiceFalse.copy(alpha = 0.12f))
                            .border(1.dp, ColorChoiceFalse.copy(alpha = 0.3f), RoundedCornerShape(50))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ColorChoiceFalse,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "انتهى وقت الإجابة",
                            color = ColorChoiceFalse,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bottom Section: 2x2 Big Answer Buttons or True/False
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (state.type == QuestionType.TRUE_FALSE) {
                    TrueFalseButtonsWarm(
                        selectedAnswer = state.submittedAnswer ?: state.selectedAnswer,
                        isEnabled = canTap,
                        onSelect = { onAnswerSelected(it) }
                    )
                } else {
                    McqButtonsGridWarm(
                        choices = state.choices.ifEmpty { listOf("أ", "ب", "ج", "د") },
                        selectedAnswer = state.submittedAnswer ?: state.selectedAnswer,
                        isEnabled = canTap,
                        onSelect = { onAnswerSelected(it) }
                    )
                }

                if (onSimulateNext != null) {
                    OutlinedButton(
                        onClick = onSimulateNext,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, WarmBorder),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmMediumBrown)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("السؤال التالي (محاكاة)", fontSize = 12.sp)
                    }
                }
            }
        }

        // Bottom Progress & Status Footer Card (Warm Organic style)
        BottomStatusFooter(
            currentQuestion = state.questionNumber,
            totalQuestions = if (state.totalQuestions > 0) state.totalQuestions else 10,
            isSubmitted = isAnswerSubmitted
        )
    }
}

@Composable
private fun McqButtonsGridWarm(
    choices: List<String>,
    selectedAnswer: String?,
    isEnabled: Boolean,
    onSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        val chunked = choices.chunked(2)
        chunked.forEach { rowChoices ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rowChoices.forEach { choiceLabel ->
                    val isSelected = selectedAnswer == choiceLabel

                    BigWarmCardButton(
                        label = choiceLabel,
                        isSelected = isSelected,
                        isEnabled = isEnabled,
                        modifier = Modifier
                            .weight(1f)
                            .height(86.dp)
                            .testTag("answer_choice_$choiceLabel"),
                        onClick = { onSelect(choiceLabel) }
                    )
                }
            }
        }
    }
}

@Composable
private fun TrueFalseButtonsWarm(
    selectedAnswer: String?,
    isEnabled: Boolean,
    onSelect: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // صح (True)
        val isTrueSelected = selectedAnswer == "صح" || selectedAnswer == "TRUE"
        BigWarmCardButton(
            label = "صح",
            isSelected = isTrueSelected,
            isEnabled = isEnabled,
            accentColor = ColorChoiceTrue,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .testTag("answer_choice_true"),
            onClick = { onSelect("صح") }
        )

        // خطأ (False)
        val isFalseSelected = selectedAnswer == "خطأ" || selectedAnswer == "FALSE"
        BigWarmCardButton(
            label = "خطأ",
            isSelected = isFalseSelected,
            isEnabled = isEnabled,
            accentColor = ColorChoiceFalse,
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .testTag("answer_choice_false"),
            onClick = { onSelect("خطأ") }
        )
    }
}

@Composable
private fun BigWarmCardButton(
    label: String,
    isSelected: Boolean,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    accentColor: Color = WarmOrangePrimary,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) accentColor else Color.White
    val borderColor = if (isSelected) accentColor else WarmBorder
    val textColor = if (isSelected) Color.White else WarmDarkBrown

    Card(
        modifier = modifier
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(24.dp)
            )
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = isEnabled, onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(if (isSelected) 2.5.dp else 2.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "تم الاختيار",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }

                Text(
                    text = label,
                    color = textColor,
                    fontSize = if (label.length > 2) 26.sp else 34.sp,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun BottomStatusFooter(
    currentQuestion: Int,
    totalQuestions: Int,
    isSubmitted: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = if (totalQuestions > 0) {
        (currentQuestion.toFloat() / totalQuestions.toFloat()).coerceIn(0f, 1f)
    } else 0.5f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.9f))
            .border(width = 1.dp, color = WarmBorder.copy(alpha = 0.5f), shape = RoundedCornerShape(0.dp))
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isSubmitted) "تم إرسال إجابتك للخادم" else "بانتظار اختيار إجابتك...",
                    color = if (isSubmitted) StatusConnectedText else WarmMediumBrown,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Text(
                    text = "$currentQuestion / $totalQuestions",
                    color = WarmDarkBrown,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(50)),
                color = WarmOrangePrimary,
                trackColor = WarmTrack,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

