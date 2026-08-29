package com.example.model

sealed interface ScreenState {
    data object Welcome : ScreenState
    data object Scanner : ScreenState
    data object WaitingRoom : ScreenState
    data class ActiveQuestion(
        val questionId: String,
        val questionNumber: Int,
        val totalQuestions: Int,
        val type: QuestionType,
        val choices: List<String>,
        val totalSeconds: Int,
        val remainingSeconds: Int,
        val allowAnswer: Boolean,
        val selectedAnswer: String? = null,
        val submittedAnswer: String? = null,
        val isConfirmed: Boolean = false
    ) : ScreenState
    data class QuestionClosed(
        val questionId: String,
        val questionNumber: Int,
        val submittedAnswer: String?,
        val message: String = "انتهى وقت الإجابة - في انتظار النتيجة"
    ) : ScreenState
    data class GameOver(
        val finalScore: Int?,
        val finalRank: Int?,
        val totalPlayers: Int?,
        val message: String
    ) : ScreenState
}

data class PlayerProfile(
    val playerId: String,
    val playerName: String
)

data class ServerConfig(
    val host: String,
    val port: Int = 8765,
    val roomId: String = "default"
) {
    val websocketUrl: String
        get() = "ws://$host:$port"
}
