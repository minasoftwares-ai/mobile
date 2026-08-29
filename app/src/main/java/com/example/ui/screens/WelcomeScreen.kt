package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SettingsEthernet
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ConnectionInfo
import com.example.model.ServerConfig
import com.example.ui.components.ConnectionBadge
import com.example.ui.components.CopticCrossOrnament
import com.example.ui.components.ManualConnectDialog
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.CreamSurfaceVariant
import com.example.ui.theme.WarmAmberAccent
import com.example.ui.theme.WarmBorder
import com.example.ui.theme.WarmDarkBrown
import com.example.ui.theme.WarmGold
import com.example.ui.theme.WarmMediumBrown
import com.example.ui.theme.WarmOrangePrimary
import com.example.ui.theme.WarmOrangeVariant

@Composable
fun WelcomeScreen(
    playerName: String,
    onPlayerNameChange: (String) -> Unit,
    connectionInfo: ConnectionInfo,
    onStartScan: () -> Unit,
    onManualConnect: (ServerConfig) -> Unit,
    onStartDemo: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showManualDialog by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Hero Header with Coptic Cross
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(WarmGold.copy(alpha = 0.25f), WarmOrangePrimary.copy(alpha = 0.1f))
                        )
                    )
                    .border(2.dp, WarmGold.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                CopticCrossOrnament(size = 56.dp, color = WarmOrangePrimary)
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "مسابقة القبطيات",
                style = MaterialTheme.typography.displayMedium,
                color = WarmDarkBrown,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Text(
                text = "جهاز تحكم وإجابة المتسابق (Player Controller)",
                style = MaterialTheme.typography.bodyMedium,
                color = WarmMediumBrown,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Input Card
            ElevatedCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(24.dp)),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = CreamSurface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "تسجيل اسم اللاعب",
                        style = MaterialTheme.typography.titleLarge,
                        color = WarmDarkBrown,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "سيظهر هذا الاسم على شاشة الكمبيوتر الرئيسية للمسابقة:",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmMediumBrown,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = playerName,
                        onValueChange = {
                            onPlayerNameChange(it)
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("اسم المتسابق / الفريق") },
                        placeholder = { Text("مثال: كيرلس ميخائيل") },
                        singleLine = true,
                        isError = nameError,
                        supportingText = {
                            if (nameError) {
                                Text("يرجى كتابة اسمك للمتابعة", color = MaterialTheme.colorScheme.error)
                            }
                        },
                        trailingIcon = {
                            if (playerName.isNotEmpty()) {
                                IconButton(onClick = { onPlayerNameChange("") }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "مسح الاسم",
                                        tint = WarmMediumBrown
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = WarmOrangePrimary,
                            unfocusedBorderColor = WarmBorder,
                            focusedLabelColor = WarmOrangePrimary,
                            cursorColor = WarmOrangePrimary
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("player_name_input")
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Primary Button: QR Scan
                    Button(
                        onClick = {
                            if (playerName.isBlank()) {
                                nameError = true
                            } else {
                                onStartScan()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("scan_qr_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarmOrangePrimary,
                            contentColor = Color.White
                        ),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "مسح رمز QR للاتصال بالكمبيوتر",
                                style = MaterialTheme.typography.labelLarge,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Button: Manual IP Entry
                    OutlinedButton(
                        onClick = {
                            if (playerName.isBlank()) {
                                nameError = true
                            } else {
                                showManualDialog = true
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("manual_connect_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmDarkBrown),
                        border = androidx.compose.foundation.BorderStroke(1.5.dp, WarmOrangePrimary.copy(alpha = 0.5f))
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SettingsEthernet,
                                contentDescription = null,
                                tint = WarmOrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "الاتصال اليدوي بعنوان IP",
                                style = MaterialTheme.typography.titleMedium,
                                color = WarmDarkBrown
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Instructions Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CreamSurfaceVariant.copy(alpha = 0.6f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = WarmOrangePrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "كيفية الاتصال بالمسابقة:",
                            fontWeight = FontWeight.Bold,
                            color = WarmDarkBrown,
                            fontSize = 15.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "1. تأكد من اتصال هاتفك بنفس شبكة Wi-Fi المحلية لجهاز الكمبيوتر.\n" +
                               "2. قم بمسح رمز QR المعروض على شاشة الكمبيوتر.\n" +
                               "3. سيعمل الهاتف كجهاز تحكم للإجابة أثناء عرض الأسئلة على الشاشة الكبيرة.",
                        style = MaterialTheme.typography.bodySmall,
                        color = WarmMediumBrown,
                        lineHeight = 20.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Demo Mode Option for Testing & Preview
            OutlinedButton(
                onClick = onStartDemo,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("demo_mode_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmMediumBrown),
                border = androidx.compose.foundation.BorderStroke(1.dp, WarmBorder)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsEsports,
                        contentDescription = null,
                        tint = WarmAmberAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "تجربة تفاعلية مباشرة (Demo Simulator)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = WarmDarkBrown
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showManualDialog) {
        ManualConnectDialog(
            initialHost = connectionInfo.hostIp,
            initialPort = connectionInfo.port,
            initialRoom = connectionInfo.roomId,
            onDismiss = { showManualDialog = false },
            onConnect = { config ->
                showManualDialog = false
                onManualConnect(config)
            }
        )
    }
}
