package com.example.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.ClientMessageBuilder
import com.example.model.ConnectionInfo
import com.example.model.ConnectionStatus
import com.example.model.QuestionType
import com.example.model.ScreenState
import com.example.model.ServerConfig
import com.example.model.ServerMessage
import com.example.network.QrCodeParser
import com.example.network.QuizWebSocketClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

class QuizViewModel(application: Application) : AndroidViewModel(application) {
    private val tag = "QuizViewModel"

    private val webSocketClient = QuizWebSocketClient(viewModelScope)

    val connectionInfo: StateFlow<ConnectionInfo> = webSocketClient.connectionInfo

    private val _playerName = MutableStateFlow("")
    val playerName: StateFlow<String> = _playerName.asStateFlow()

    private val _playerId = MutableStateFlow("p_${UUID.randomUUID().toString().take(8)}")
    val playerId: StateFlow<String> = _playerId.asStateFlow()

    private val _screenState = MutableStateFlow<ScreenState>(ScreenState.Welcome)
    val screenState: StateFlow<ScreenState> = _screenState.asStateFlow()

    private val _currentScore = MutableStateFlow<Int?>(null)
    val currentScore: StateFlow<Int?> = _currentScore.asStateFlow()

    private val _currentRank = MutableStateFlow<Int?>(null)
    val currentRank: StateFlow<Int?> = _currentRank.asStateFlow()

    private val _totalPlayers = MutableStateFlow<Int?>(null)
    val totalPlayers: StateFlow<Int?> = _totalPlayers.asStateFlow()

    private val _roomTitle = MutableStateFlow("مسابقة القبطيات")
    val roomTitle: StateFlow<String> = _roomTitle.asStateFlow()

    // Map of questionId -> submitted answer to prevent double submissions
    private val submittedAnswersMap = mutableMapOf<String, String>()

    private var countdownJob: Job? = null

    init {
        // Collect incoming messages from WebSocket
        viewModelScope.launch {
            webSocketClient.incomingMessages.collect { message ->
                handleServerMessage(message)
            }
        }

        // Monitor connection status changes
        viewModelScope.launch {
            connectionInfo.collect { info ->
                if (info.status == ConnectionStatus.DISCONNECTED && _screenState.value !is ScreenState.Welcome) {
                    // Do not force welcome screen immediately if reconnecting, keep state
                }
            }
        }
    }

    fun setPlayerName(name: String) {
        _playerName.value = name.trim()
    }

    fun navigateTo(screen: ScreenState) {
        _screenState.value = screen
    }

    fun onQrScanned(qrData: String) {
        val config = QrCodeParser.parse(qrData)
        if (config != null) {
            connectToServer(config)
        } else {
            Log.e(tag, "Invalid QR code scanned: $qrData")
        }
    }

    fun connectToServer(config: ServerConfig) {
        val name = _playerName.value.ifBlank { "لاعب" }
        _screenState.value = ScreenState.WaitingRoom
        webSocketClient.connect(config, _playerId.value, name)
    }

    fun submitAnswer(questionId: String, answer: String) {
        val currentScreen = _screenState.value
        if (currentScreen is ScreenState.ActiveQuestion && currentScreen.questionId == questionId) {
            if (!currentScreen.allowAnswer) {
                Log.w(tag, "Answering is closed for this question")
                return
            }

            // Anti-double submission check
            if (submittedAnswersMap.containsKey(questionId)) {
                Log.w(tag, "Answer already submitted for $questionId: ${submittedAnswersMap[questionId]}")
                return
            }

            // Record submitted answer locally
            submittedAnswersMap[questionId] = answer

            // Update UI state immediately to show selected & sent
            _screenState.value = currentScreen.copy(
                selectedAnswer = answer,
                submittedAnswer = answer,
                isConfirmed = true
            )

            // Send to PC Host via WebSocket
            val jsonPayload = ClientMessageBuilder.buildSubmitAnswerMessage(
                playerId = _playerId.value,
                playerName = _playerName.value.ifBlank { "لاعب" },
                questionId = questionId,
                answer = answer
            )
            webSocketClient.sendMessage(jsonPayload)
        }
    }

    private fun handleServerMessage(message: ServerMessage) {
        Log.d(tag, "Handling server message: $message")
        when (message) {
            is ServerMessage.JoinAck -> {
                message.roomName?.let { _roomTitle.value = it }
                if (_screenState.value is ScreenState.Welcome || _screenState.value is ScreenState.Scanner) {
                    _screenState.value = ScreenState.WaitingRoom
                }
            }

            is ServerMessage.WaitingRoom -> {
                message.playerCount?.let { _totalPlayers.value = it }
                if (_screenState.value !is ScreenState.GameOver) {
                    _screenState.value = ScreenState.WaitingRoom
                }
            }

            is ServerMessage.QuestionStart -> {
                // SECURITY: Note that we DO NOT receive or store question text or answer explanations!
                countdownJob?.cancel()

                // Check if already answered previously (e.g., reconnect scenario)
                val existingAnswer = submittedAnswersMap[message.questionId]

                _screenState.value = ScreenState.ActiveQuestion(
                    questionId = message.questionId,
                    questionNumber = message.questionNumber,
                    totalQuestions = message.totalQuestions,
                    type = message.questionType,
                    choices = message.choices,
                    totalSeconds = message.totalSeconds,
                    remainingSeconds = message.timeRemaining,
                    allowAnswer = message.allowAnswer && (existingAnswer == null),
                    selectedAnswer = existingAnswer,
                    submittedAnswer = existingAnswer,
                    isConfirmed = existingAnswer != null
                )

                // Start local smooth countdown timer
                startCountdown(message.questionId, message.timeRemaining)
            }

            is ServerMessage.TimerSync -> {
                val current = _screenState.value
                if (current is ScreenState.ActiveQuestion && current.questionId == message.questionId) {
                    val isLocked = submittedAnswersMap.containsKey(message.questionId)
                    _screenState.value = current.copy(
                        remainingSeconds = message.timeRemaining,
                        allowAnswer = message.allowAnswer && !isLocked
                    )
                }
            }

            is ServerMessage.QuestionClosed -> {
                countdownJob?.cancel()
                val current = _screenState.value
                if (current is ScreenState.ActiveQuestion && current.questionId == message.questionId) {
                    _screenState.value = ScreenState.QuestionClosed(
                        questionId = message.questionId,
                        questionNumber = current.questionNumber,
                        submittedAnswer = current.submittedAnswer,
                        message = message.message
                    )
                }
            }

            is ServerMessage.AnswerReceived -> {
                val current = _screenState.value
                if (current is ScreenState.ActiveQuestion && current.questionId == message.questionId) {
                    _screenState.value = current.copy(isConfirmed = true)
                }
            }

            is ServerMessage.ScoreUpdate -> {
                _currentScore.value = message.score
                message.rank?.let { _currentRank.value = it }
                message.totalPlayers?.let { _totalPlayers.value = it }
            }

            is ServerMessage.GameOver -> {
                countdownJob?.cancel()
                _screenState.value = ScreenState.GameOver(
                    finalScore = message.finalScore ?: _currentScore.value,
                    finalRank = message.finalRank ?: _currentRank.value,
                    totalPlayers = message.totalPlayers ?: _totalPlayers.value,
                    message = message.message
                )
            }

            is ServerMessage.ResetToLobby -> {
                countdownJob?.cancel()
                submittedAnswersMap.clear()
                _screenState.value = ScreenState.WaitingRoom
            }

            is ServerMessage.Unknown -> {
                Log.w(tag, "Unknown message received: ${message.rawText}")
            }
        }
    }

    private fun startCountdown(questionId: String, initialSeconds: Int) {
        countdownJob?.cancel()
        if (initialSeconds <= 0) return

        countdownJob = viewModelScope.launch {
            var secondsLeft = initialSeconds
            while (isActive && secondsLeft > 0) {
                delay(1000L)
                secondsLeft--
                val current = _screenState.value
                if (current is ScreenState.ActiveQuestion && current.questionId == questionId) {
                    val allow = (secondsLeft > 0) && !submittedAnswersMap.containsKey(questionId)
                    _screenState.value = current.copy(
                        remainingSeconds = secondsLeft,
                        allowAnswer = allow
                    )
                } else {
                    break
                }
            }
        }
    }

    fun disconnect() {
        countdownJob?.cancel()
        webSocketClient.disconnect()
        _screenState.value = ScreenState.Welcome
    }

    // ==========================================
    // DEMO / SIMULATION MODE FOR TESTING
    // ==========================================
    fun startDemoMode() {
        viewModelScope.launch {
            _screenState.value = ScreenState.WaitingRoom
            delay(1500)
            // Start Question 1: Multiple Choice
            handleServerMessage(
                ServerMessage.QuestionStart(
                    questionId = "demo_q1",
                    questionNumber = 1,
                    totalQuestions = 5,
                    questionType = QuestionType.MCQ,
                    choices = listOf("أ", "ب", "ج", "د"),
                    totalSeconds = 25,
                    timeRemaining = 25,
                    allowAnswer = true
                )
            )
        }
    }

    fun triggerDemoNextQuestion() {
        val current = _screenState.value
        val nextNum = if (current is ScreenState.ActiveQuestion) current.questionNumber + 1 else 1
        when (nextNum % 3) {
            1 -> {
                handleServerMessage(
                    ServerMessage.QuestionStart(
                        questionId = "demo_q$nextNum",
                        questionNumber = nextNum,
                        totalQuestions = 5,
                        questionType = QuestionType.MCQ,
                        choices = listOf("أ", "ب", "ج", "د"),
                        totalSeconds = 20,
                        timeRemaining = 20,
                        allowAnswer = true
                    )
                )
            }
            2 -> {
                handleServerMessage(
                    ServerMessage.QuestionStart(
                        questionId = "demo_q$nextNum",
                        questionNumber = nextNum,
                        totalQuestions = 5,
                        questionType = QuestionType.TRUE_FALSE,
                        choices = listOf("صح", "خطأ"),
                        totalSeconds = 15,
                        timeRemaining = 15,
                        allowAnswer = true
                    )
                )
            }
            0 -> {
                handleServerMessage(
                    ServerMessage.QuestionStart(
                        questionId = "demo_q$nextNum",
                        questionNumber = nextNum,
                        totalQuestions = 5,
                        questionType = QuestionType.EXPLAIN_TEXT,
                        choices = emptyList(),
                        totalSeconds = 45,
                        timeRemaining = 45,
                        allowAnswer = true
                    )
                )
            }
        }
    }

    fun triggerDemoGameOver() {
        handleServerMessage(
            ServerMessage.GameOver(
                finalScore = 90,
                finalRank = 1,
                totalPlayers = 18,
                message = "مبروك! انتهت مسابقة القبطيات بنجاح"
            )
        )
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        webSocketClient.disconnect()
    }
}
