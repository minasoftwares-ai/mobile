package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionInfo
import com.example.model.ConnectionStatus
import com.example.model.ServerConfig
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.StatusConnected
import com.example.ui.theme.StatusConnectedBg
import com.example.ui.theme.StatusConnectedBorder
import com.example.ui.theme.StatusConnectedDot
import com.example.ui.theme.StatusConnectedText
import com.example.ui.theme.StatusDisconnected
import com.example.ui.theme.StatusReconnecting
import com.example.ui.theme.WarmAmberAccent
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmDarkBrown
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmMediumBrown
import com.example.ui.theme.WarmOrangePrimary
import com.example.ui.theme.WarmTrack

@Composable
fun CopticCrossOrnament(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    color: Color = WarmGold
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w / 2f
        val cy = h / 2f
        val stroke = w * 0.12f

        // Central cross arms
        drawLine(
            color = color,
            start = Offset(cx, h * 0.15f),
            end = Offset(cx, h * 0.85f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = color,
            start = Offset(w * 0.15f, cy),
            end = Offset(w * 0.85f, cy),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )

        // Four small decorative Coptic trefoil dots
        val d = w * 0.22f
        drawCircle(color = color, radius = stroke * 0.55f, center = Offset(cx - d, cy - d))
        drawCircle(color = color, radius = stroke * 0.55f, center = Offset(cx + d, cy - d))
        drawCircle(color = color, radius = stroke * 0.55f, center = Offset(cx - d, cy + d))
        drawCircle(color = color, radius = stroke * 0.55f, center = Offset(cx + d, cy + d))
    }
}

@Composable
fun ConnectionBadge(
    connectionInfo: ConnectionInfo,
    modifier: Modifier = Modifier
) {
    val isConnected = connectionInfo.status == ConnectionStatus.CONNECTED

    val (bgColor, borderColor, textColor, text) = when (connectionInfo.status) {
        ConnectionStatus.CONNECTED -> Quadruple(
            StatusConnectedBg,
            StatusConnectedBorder,
            StatusConnectedText,
            "متصل بالخادم"
        )
        ConnectionStatus.CONNECTING -> Quadruple(
            WarmAmberAccent.copy(alpha = 0.2f),
            WarmBorder,
            WarmOrangePrimary,
            "جارٍ الاتصال..."
        )
        ConnectionStatus.RECONNECTING -> Quadruple(
            WarmAmberAccent.copy(alpha = 0.25f),
            WarmBorder,
            WarmOrangePrimary,
            "إعادة الاتصال..."
        )
        ConnectionStatus.ERROR, ConnectionStatus.DISCONNECTED -> Quadruple(
            StatusDisconnected.copy(alpha = 0.12f),
            StatusDisconnected.copy(alpha = 0.3f),
            StatusDisconnected,
            "غير متصل"
        )
    }

    val infiniteTransition = rememberInfiniteTransition(label = "badge_sync")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_dot"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        if (isConnected) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(StatusConnectedDot.copy(alpha = dotAlpha))
            )
        } else {
            Icon(
                imageVector = when (connectionInfo.status) {
                    ConnectionStatus.CONNECTING, ConnectionStatus.RECONNECTING -> Icons.Default.Sync
                    else -> Icons.Default.WifiOff
                },
                contentDescription = text,
                tint = textColor,
                modifier = Modifier.size(13.dp)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
fun TopPlayerHeader(
    playerName: String,
    connectionInfo: ConnectionInfo,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.95f))
            .border(
                width = 1.dp,
                color = WarmBorder.copy(alpha = 0.6f),
                shape = RoundedCornerShape(0.dp)
            )
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.Start) {
                Text(
                    text = "اللاعب",
                    color = WarmOrangePrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = playerName.ifBlank { "لاعب مسابقة" },
                    color = WarmDarkBrown,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            ConnectionBadge(connectionInfo = connectionInfo)
        }
    }
}

@Composable
fun ReconnectingBanner(
    isReconnecting: Boolean,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isReconnecting,
        enter = slideInVertically() + fadeIn(),
        exit = slideOutVertically() + fadeOut(),
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(WarmOrangePrimary)
                .padding(vertical = 8.dp, horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "banner_sync")
                val rotation by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "banner_rot"
                )
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier
                        .size(18.dp)
                        .rotate(rotation)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "انقطع الاتصال، جارٍ إعادة الاتصال بالخادم...",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
fun CircularCountdownTimer(
    totalSeconds: Int,
    remainingSeconds: Int,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    val progress = if (totalSeconds > 0) {
        (remainingSeconds.toFloat() / totalSeconds.toFloat()).coerceIn(0f, 1f)
    } else 0f

    val isUrgent = remainingSeconds in 1..5
    val infiniteTransition = rememberInfiniteTransition(label = "pulse_timer")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUrgent) 1.06f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val timerColor = when {
        remainingSeconds <= 5 -> Color(0xFFD32F2F)
        else -> WarmOrangePrimary
    }

    Box(
        modifier = modifier
            .size(size)
            .scale(pulseScale),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val strokeWidth = 8.dp.toPx()
            val canvasSize = this.size.width
            val radius = (canvasSize - strokeWidth) / 2f

            // Warm Organic Background Circle track (#F9C784)
            drawCircle(
                color = WarmBorder,
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Active remaining arc in Warm Terracotta (#FF8C42)
            drawArc(
                color = timerColor,
                startAngle = -90f,
                sweepAngle = -progress * 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$remainingSeconds",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = WarmDarkBrown,
                textAlign = TextAlign.Center
            )
            Text(
                text = "ثانية متبقية",
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = WarmMediumBrown
            )
        }
    }
}

@Composable
fun ManualConnectDialog(
    initialHost: String,
    initialPort: Int,
    initialRoom: String,
    onDismiss: () -> Unit,
    onConnect: (ServerConfig) -> Unit
) {
    var host by remember { mutableStateOf(initialHost.ifBlank { "192.168.1." }) }
    var port by remember { mutableStateOf(initialPort.toString()) }
    var room by remember { mutableStateOf(initialRoom.ifBlank { "coptic-quiz" }) }
    var errorText by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Wifi, contentDescription = null, tint = WarmOrangePrimary)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "الاتصال المباشر بالخادم",
                    fontWeight = FontWeight.Bold,
                    color = WarmDarkBrown
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "أدخل عنوان IP الخاص بجهاز الكمبيوتر المعروض في برنامج المسابقة:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = WarmDarkBrown.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = host,
                    onValueChange = { host = it; errorText = null },
                    label = { Text("عنوان IP الخادم") },
                    placeholder = { Text("مثال: 192.168.1.50") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("manual_ip_input")
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = port,
                        onValueChange = { port = it },
                        label = { Text("المنفذ (Port)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = room,
                        onValueChange = { room = it },
                        label = { Text("رقم الغرفة") },
                        singleLine = true,
                        modifier = Modifier.weight(1.3f)
                    )
                }

                if (errorText != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = errorText!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanHost = host.trim()
                    val cleanPort = port.toIntOrNull() ?: 8765
                    if (cleanHost.isBlank() || cleanHost == "192.168.1.") {
                        errorText = "يرجى كتابة عنوان IP صالح"
                        return@Button
                    }
                    onConnect(ServerConfig(host = cleanHost, port = cleanPort, roomId = room.trim()))
                },
                colors = ButtonDefaults.buttonColors(containerColor = WarmOrangePrimary),
                modifier = Modifier.testTag("connect_confirm_button")
            ) {
                Text("اتصال", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("إلغاء", color = WarmDarkBrown)
            }
        }
    )
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
