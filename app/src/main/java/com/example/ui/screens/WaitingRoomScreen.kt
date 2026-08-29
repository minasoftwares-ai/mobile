package com.example.ui.screens

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionInfo
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.CopticCrossOrnament
import com.example.ui.components.ReconnectingBanner
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.WarmAmberAccent
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmDarkBrown
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmMediumBrown
import com.example.ui.theme.WarmOrangePrimary

@Composable
fun WaitingRoomScreen(
    playerName: String,
    roomTitle: String,
    connectionInfo: ConnectionInfo,
    totalPlayers: Int?,
    onDisconnect: () -> Unit,
    onSimulateQuestion: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_waiting")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
    ) {
        ReconnectingBanner(isReconnecting = connectionInfo.isReconnecting)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Player Tag & Connection status
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    ConnectionBadge(connectionInfo = connectionInfo)

                    OutlinedButton(
                        onClick = onDisconnect,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmMediumBrown),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                    ) {
                        Text("خروج", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Player Card Banner
                ElevatedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = CreamSurface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(WarmOrangePrimary.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = WarmOrangePrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = "المتسابق",
                                style = MaterialTheme.typography.bodySmall,
                                color = WarmMediumBrown
                            )
                            Text(
                                text = playerName.ifBlank { "لاعب مسابقة" },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = WarmDarkBrown
                            )
                        }
                    }
                }
            }

            // Center Section: Big Waiting Indicator
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 30.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(130.dp)
                        .scale(pulseScale)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(WarmGold.copy(alpha = 0.35f), WarmOrangePrimary.copy(alpha = 0.12f))
                            )
                        )
                        .border(3.dp, WarmGold.copy(alpha = 0.5f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    CopticCrossOrnament(size = 80.dp, color = WarmOrangePrimary)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "في انتظار بدء المسابقة",
                    style = MaterialTheme.typography.headlineLarge,
                    color = WarmDarkBrown,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("waiting_room_title")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "سيتحكم الكمبيوتر المضيف في تشغيل الأسئلة",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmMediumBrown,
                    textAlign = TextAlign.Center
                )

                if (totalPlayers != null && totalPlayers > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(CreamSurfaceVariant)
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "عدد المتسابقين المتصلين: $totalPlayers",
                            color = WarmDarkBrown,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Bottom Section: Anti-Cheating & Reminder Notice
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant.copy(alpha = 0.85f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = null,
                            tint = WarmOrangePrimary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "انظر إلى شاشة العرض الرئيسية لقراءة السؤال. ستظهر أزرار الإجابة على هاتفك تلقائياً فور بدء السؤال.",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarmDarkBrown,
                            lineHeight = 19.sp
                        )
                    }
                }

                if (onSimulateQuestion != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = onSimulateQuestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("simulate_next_q_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, WarmAmberAccent)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = WarmOrangePrimary)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("تجربة إطلاق سؤال محاكاة", color = WarmOrangePrimary)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
