package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionInfo
import com.example.model.ScreenState
import com.example.ui.components.CircularCountdownTimer
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.ReconnectingBanner
import com.example.ui.components.TopPlayerHeader
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.WarmAmberAccent
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmDarkBrown
import com.example.ui.theme.WarmMediumBrown
import com.example.ui.theme.WarmOrangePrimary
import com.example.ui.theme.WarmOrangeVariant

@Composable
fun AnswerTextScreen(
    state: ScreenState.ActiveQuestion,
    connectionInfo: ConnectionInfo,
    playerName: String = "",
    onSubmitText: (String) -> Unit,
    onSimulateNext: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var textAnswer by remember(state.questionId) {
        mutableStateOf(state.submittedAnswer ?: state.selectedAnswer ?: "")
    }

    val isSubmitted = state.submittedAnswer != null
    val canSubmit = state.allowAnswer && !isSubmitted && state.remainingSeconds > 0 && textAnswer.isNotBlank()
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .imePadding()
    ) {
        ReconnectingBanner(isReconnecting = connectionInfo.isReconnecting)

        TopPlayerHeader(
            playerName = playerName,
            connectionInfo = connectionInfo
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: Question Number & Badge
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(WarmOrangePrimary)
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EditNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (state.totalQuestions > 0)
                                    "السؤال ${state.questionNumber} (بم تفسر)"
                                else
                                    "السؤال رقم ${state.questionNumber} (بم تفسر)",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Timer & Anti-Cheat Info
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularCountdownTimer(
                        totalSeconds = state.totalSeconds,
                        remainingSeconds = state.remainingSeconds,
                        size = 80.dp
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = WarmOrangePrimary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "اقرأ السؤال على الشاشة ثم اكتب إجابتك هنا:",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmDarkBrown,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Text Input Area
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CreamSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "كتابة الإجابة (بم تفسر / علل / اذكر):",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = WarmDarkBrown
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = textAnswer,
                        onValueChange = {
                            if (!isSubmitted && state.remainingSeconds > 0) {
                                textAnswer = it
                            }
                        },
                        placeholder = {
                            Text(
                                text = if (isSubmitted) "تم إرسال الإجابة..." else "اكتب شرحك وإجابتك هنا بالتفصيل...",
                                color = WarmMediumBrown.copy(alpha = 0.6f)
                            )
                        },
                        enabled = !isSubmitted && state.remainingSeconds > 0,
                        minLines = 5,
                        maxLines = 8,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmOrangePrimary,
                            unfocusedBorderColor = WarmBorder,
                            focusedTextColor = WarmDarkBrown,
                            unfocusedTextColor = WarmDarkBrown,
                            cursorColor = WarmOrangePrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("text_answer_input")
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${textAnswer.length} حرف",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmMediumBrown
                        )

                        if (isSubmitted) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = StatusConnected,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "تم إرسال الإجابة بنجاح",
                                    color = StatusConnected,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Submit Button
            Column(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onSubmitText(textAnswer.trim())
                    },
                    enabled = canSubmit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp)
                        .testTag("submit_text_answer_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrangePrimary,
                        disabledContainerColor = if (isSubmitted) StatusConnected else WarmOrangePrimary.copy(alpha = 0.4f),
                        contentColor = Color.White,
                        disabledContentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = if (isSubmitted) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isSubmitted) "تم إرسال الإجابة" else "إرسال الإجابة إلى الكمبيوتر",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 16.sp
                        )
                    }
                }

                if (onSimulateNext != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onSimulateNext,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("السؤال التالي (محاكاة)", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
