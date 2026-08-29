package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.QuestionType
import com.example.model.ScreenState
import com.example.ui.screens.AnswerMcqScreen
import com.example.ui.screens.AnswerTextScreen
import com.example.ui.screens.FinishedScreen
import com.example.ui.screens.QrScannerScreen
import com.example.ui.screens.QuestionClosedScreen
import com.example.ui.screens.WaitingRoomScreen
import com.example.ui.screens.WelcomeScreen
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.QuizViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: QuizViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                // Arabic RTL Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .safeDrawingPadding(),
                        color = CreamBackground
                    ) {
                        QuizApp(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun QuizApp(viewModel: QuizViewModel) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val playerName by viewModel.playerName.collectAsStateWithLifecycle()
    val connectionInfo by viewModel.connectionInfo.collectAsStateWithLifecycle()
    val totalPlayers by viewModel.totalPlayers.collectAsStateWithLifecycle()
    val roomTitle by viewModel.roomTitle.collectAsStateWithLifecycle()

    // Handle Android hardware back button
    BackHandler(enabled = screenState !is ScreenState.Welcome) {
        when (screenState) {
            is ScreenState.Scanner -> viewModel.navigateTo(ScreenState.Welcome)
            is ScreenState.WaitingRoom, is ScreenState.GameOver -> viewModel.disconnect()
            else -> {
                // In active question, avoid accidental back exit
            }
        }
    }

    AnimatedContent(
        targetState = screenState,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "screen_transition"
    ) { currentScreen ->
        when (currentScreen) {
            is ScreenState.Welcome -> {
                WelcomeScreen(
                    playerName = playerName,
                    onPlayerNameChange = { viewModel.setPlayerName(it) },
                    connectionInfo = connectionInfo,
                    onStartScan = { viewModel.navigateTo(ScreenState.Scanner) },
                    onManualConnect = { config -> viewModel.connectToServer(config) },
                    onStartDemo = { viewModel.startDemoMode() }
                )
            }

            is ScreenState.Scanner -> {
                QrScannerScreen(
                    onQrScanned = { qrData -> viewModel.onQrScanned(qrData) },
                    onBack = { viewModel.navigateTo(ScreenState.Welcome) },
                    onManualConnect = { config -> viewModel.connectToServer(config) }
                )
            }

            is ScreenState.WaitingRoom -> {
                WaitingRoomScreen(
                    playerName = playerName,
                    roomTitle = roomTitle,
                    connectionInfo = connectionInfo,
                    totalPlayers = totalPlayers,
                    onDisconnect = { viewModel.disconnect() },
                    onSimulateQuestion = { viewModel.triggerDemoNextQuestion() }
                )
            }

            is ScreenState.ActiveQuestion -> {
                if (currentScreen.type == QuestionType.EXPLAIN_TEXT) {
                    AnswerTextScreen(
                        state = currentScreen,
                        connectionInfo = connectionInfo,
                        playerName = playerName,
                        onSubmitText = { answer -> viewModel.submitAnswer(currentScreen.questionId, answer) },
                        onSimulateNext = { viewModel.triggerDemoNextQuestion() }
                    )
                } else {
                    AnswerMcqScreen(
                        state = currentScreen,
                        connectionInfo = connectionInfo,
                        playerName = playerName,
                        onAnswerSelected = { answer -> viewModel.submitAnswer(currentScreen.questionId, answer) },
                        onSimulateNext = { viewModel.triggerDemoNextQuestion() }
                    )
                }
            }

            is ScreenState.QuestionClosed -> {
                QuestionClosedScreen(
                    state = currentScreen,
                    connectionInfo = connectionInfo,
                    playerName = playerName,
                    onSimulateNext = { viewModel.triggerDemoNextQuestion() }
                )
            }

            is ScreenState.GameOver -> {
                FinishedScreen(
                    state = currentScreen,
                    playerName = playerName,
                    onReturnHome = { viewModel.disconnect() }
                )
            }
        }
    }
}
