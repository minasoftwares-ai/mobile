package com.example

import com.example.model.ClientMessageBuilder
import com.example.model.ServerMessage
import com.example.network.QrCodeParser
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleUnitTest {
    @Test
    fun qrParser_parsesWebSocketUrl() {
        val raw = "ws://192.168.1.45:8765/ws/coptic-quiz"
        val config = QrCodeParser.parse(raw)
        assertNotNull(config)
        assertEquals("192.168.1.45", config?.host)
        assertEquals(8765, config?.port)
        assertEquals("ws://192.168.1.45:8765", config?.websocketUrl)
    }

    @Test
    fun qrParser_parsesJsonPayload() {
        val raw = """{"ip":"192.168.1.88","port":9000,"room":"room123"}"""
        val config = QrCodeParser.parse(raw)
        assertNotNull(config)
        assertEquals("192.168.1.88", config?.host)
        assertEquals(9000, config?.port)
        assertEquals("room123", config?.roomId)
    }

    @Test
    fun qrParser_parsesIpAndPort() {
        val raw = "192.168.1.200:8765"
        val config = QrCodeParser.parse(raw)
        assertNotNull(config)
        assertEquals("192.168.1.200", config?.host)
        assertEquals(8765, config?.port)
    }

    @Test
    fun clientMessageBuilder_buildsJoinMessage() {
        val jsonStr = ClientMessageBuilder.buildJoinMessage(
            playerId = "player-1",
            playerName = "كيرلس",
            roomId = "coptic-quiz"
        )
        val json = JSONObject(jsonStr)
        assertEquals("JOIN", json.optString("type"))
        assertEquals("player-1", json.optString("player_id"))
        assertEquals("كيرلس", json.optString("player_name"))
        assertTrue(json.optLong("timestamp") > 0)
    }

    @Test
    fun clientMessageBuilder_buildsSubmitAnswerMessage() {
        val jsonStr = ClientMessageBuilder.buildSubmitAnswerMessage(
            playerId = "player-1",
            playerName = "كيرلس",
            questionId = "q-42",
            answer = "أ"
        )
        val json = JSONObject(jsonStr)
        assertEquals("SUBMIT_ANSWER", json.optString("type"))
        assertEquals("q-42", json.optString("question_id"))
        assertEquals("أ", json.optString("answer"))
    }

    @Test
    fun serverMessage_parsesQuestionStart() {
        val jsonStr = """{"type":"QUESTION_START","question_id":"q1","question_number":1,"question_type":"MCQ","choices":["أ","ب","ج","د"],"time_seconds":30,"time_remaining":30,"allow_answer":true}"""
        val message = ServerMessage.parse(jsonStr)
        assertTrue(message is ServerMessage.QuestionStart)
        val q = message as ServerMessage.QuestionStart
        assertEquals("q1", q.questionId)
        assertEquals(1, q.questionNumber)
        assertEquals(4, q.choices.size)
        assertEquals(30, q.totalSeconds)
    }
}


