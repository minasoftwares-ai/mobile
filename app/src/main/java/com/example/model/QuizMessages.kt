package com.example.model

import org.json.JSONArray
import org.json.JSONObject

sealed class ServerMessage {
    data class JoinAck(
        val playerId: String,
        val playerName: String,
        val roomName: String?,
        val message: String?
    ) : ServerMessage()

    data class WaitingRoom(
        val message: String,
        val playerCount: Int?
    ) : ServerMessage()

    data class QuestionStart(
        val questionId: String,
        val questionNumber: Int,
        val totalQuestions: Int,
        val questionType: QuestionType,
        val choices: List<String>,
        val totalSeconds: Int,
        val timeRemaining: Int,
        val allowAnswer: Boolean
    ) : ServerMessage()

    data class TimerSync(
        val questionId: String,
        val timeRemaining: Int,
        val allowAnswer: Boolean
    ) : ServerMessage()

    data class QuestionClosed(
        val questionId: String,
        val message: String
    ) : ServerMessage()

    data class AnswerReceived(
        val questionId: String,
        val answer: String,
        val status: String
    ) : ServerMessage()

    data class ScoreUpdate(
        val score: Int,
        val rank: Int?,
        val totalPlayers: Int?
    ) : ServerMessage()

    data class GameOver(
        val finalScore: Int?,
        val finalRank: Int?,
        val totalPlayers: Int?,
        val message: String
    ) : ServerMessage()

    data class ResetToLobby(
        val message: String
    ) : ServerMessage()

    data class Unknown(val rawText: String) : ServerMessage()

    companion object {
        fun parse(jsonStr: String): ServerMessage {
            return try {
                val json = JSONObject(jsonStr)
                val type = json.optString("type", "").uppercase()
                when (type) {
                    "JOIN_ACK", "WELCOME", "CONNECTED" -> {
                        JoinAck(
                            playerId = json.optString("player_id", ""),
                            playerName = json.optString("player_name", ""),
                            roomName = json.optString("room_name", null),
                            message = json.optString("message", "تم الاتصال بنجاح")
                        )
                    }
                    "WAITING_ROOM", "LOBBY" -> {
                        WaitingRoom(
                            message = json.optString("message", "في انتظار بدء المسابقة"),
                            playerCount = if (json.has("player_count")) json.getInt("player_count") else null
                        )
                    }
                    "QUESTION_START", "START_QUESTION", "NEW_QUESTION" -> {
                        val choicesArray = json.optJSONArray("choices")
                        val choices = mutableListOf<String>()
                        if (choicesArray != null) {
                            for (i in 0 until choicesArray.length()) {
                                choices.add(choicesArray.getString(i))
                            }
                        }
                        val rawType = json.optString("question_type", json.optString("type_name", "MCQ"))
                        val qType = QuestionType.fromString(rawType)

                        // Default choices if not supplied:
                        val resolvedChoices = if (choices.isNotEmpty()) {
                            choices
                        } else when (qType) {
                            QuestionType.MCQ -> listOf("أ", "ب", "ج", "د")
                            QuestionType.TRUE_FALSE -> listOf("صح", "خطأ")
                            QuestionType.EXPLAIN_TEXT -> emptyList()
                        }

                        val totalSecs = json.optInt("time_seconds", json.optInt("duration", 30))
                        val remaining = json.optInt("time_remaining", totalSecs)

                        QuestionStart(
                            questionId = json.optString("question_id", "q_${System.currentTimeMillis()}"),
                            questionNumber = json.optInt("question_number", 1),
                            totalQuestions = json.optInt("total_questions", 0),
                            questionType = qType,
                            choices = resolvedChoices,
                            totalSeconds = totalSecs,
                            timeRemaining = remaining,
                            allowAnswer = json.optBoolean("allow_answer", true)
                        )
                    }
                    "TIMER_SYNC", "TICK" -> {
                        TimerSync(
                            questionId = json.optString("question_id", ""),
                            timeRemaining = json.optInt("time_remaining", 0),
                            allowAnswer = json.optBoolean("allow_answer", true)
                        )
                    }
                    "QUESTION_CLOSED", "STOP_ANSWERING", "TIME_UP" -> {
                        QuestionClosed(
                            questionId = json.optString("question_id", ""),
                            message = json.optString("message", "انتهى وقت الإجابة - في انتظار النتيجة")
                        )
                    }
                    "ANSWER_RECEIVED", "ANSWER_ACK" -> {
                        AnswerReceived(
                            questionId = json.optString("question_id", ""),
                            answer = json.optString("answer", ""),
                            status = json.optString("status", "CONFIRMED")
                        )
                    }
                    "SCORE_UPDATE" -> {
                        ScoreUpdate(
                            score = json.optInt("score", 0),
                            rank = if (json.has("rank")) json.getInt("rank") else null,
                            totalPlayers = if (json.has("total_players")) json.getInt("total_players") else null
                        )
                    }
                    "GAME_OVER", "FINISH", "COMPETITION_ENDED" -> {
                        GameOver(
                            finalScore = if (json.has("final_score")) json.getInt("final_score") else if (json.has("score")) json.getInt("score") else null,
                            finalRank = if (json.has("final_rank")) json.getInt("final_rank") else if (json.has("rank")) json.getInt("rank") else null,
                            totalPlayers = if (json.has("total_players")) json.getInt("total_players") else null,
                            message = json.optString("message", "انتهت المسابقة! مبارك لجميع المشاركين")
                        )
                    }
                    "RESET_TO_LOBBY", "LOBBY_RESET" -> {
                        ResetToLobby(
                            message = json.optString("message", "تمت إعادة المسابقة إلى غرفة الانتظار")
                        )
                    }
                    else -> Unknown(jsonStr)
                }
            } catch (e: Exception) {
                Unknown(jsonStr)
            }
        }
    }
}

object ClientMessageBuilder {
    fun buildJoinMessage(playerId: String, playerName: String, roomId: String): String {
        val obj = JSONObject().apply {
            put("type", "JOIN")
            put("player_id", playerId)
            put("player_name", playerName)
            put("room_id", roomId)
            put("timestamp", System.currentTimeMillis())
        }
        return obj.toString()
    }

    fun buildReconnectMessage(playerId: String, playerName: String, roomId: String): String {
        val obj = JSONObject().apply {
            put("type", "RECONNECT")
            put("player_id", playerId)
            put("player_name", playerName)
            put("room_id", roomId)
            put("timestamp", System.currentTimeMillis())
        }
        return obj.toString()
    }

    fun buildSubmitAnswerMessage(
        playerId: String,
        playerName: String,
        questionId: String,
        answer: String
    ): String {
        val obj = JSONObject().apply {
            put("type", "SUBMIT_ANSWER")
            put("player_id", playerId)
            put("player_name", playerName)
            put("question_id", questionId)
            put("answer", answer)
            put("answer_timestamp", System.currentTimeMillis())
        }
        return obj.toString()
    }

    fun buildPingMessage(playerId: String): String {
        val obj = JSONObject().apply {
            put("type", "PING")
            put("player_id", playerId)
            put("timestamp", System.currentTimeMillis())
        }
        return obj.toString()
    }
}
