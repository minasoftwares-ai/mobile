package com.example.network

import android.util.Log
import com.example.model.ClientMessageBuilder
import com.example.model.ConnectionInfo
import com.example.model.ConnectionStatus
import com.example.model.ServerConfig
import com.example.model.ServerMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class QuizWebSocketClient(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) {
    private val tag = "QuizWebSocketClient"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .pingInterval(10, TimeUnit.SECONDS)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for websockets
        .build()

    private var webSocket: WebSocket? = null
    private var currentConfig: ServerConfig? = null
    private var currentPlayerId: String? = null
    private var currentPlayerName: String? = null

    private var reconnectJob: Job? = null
    private var isManualDisconnect = false

    private val _connectionInfo = MutableStateFlow(ConnectionInfo())
    val connectionInfo: StateFlow<ConnectionInfo> = _connectionInfo.asStateFlow()

    private val _incomingMessages = MutableSharedFlow<ServerMessage>(extraBufferCapacity = 64)
    val incomingMessages: SharedFlow<ServerMessage> = _incomingMessages.asSharedFlow()

    fun connect(config: ServerConfig, playerId: String, playerName: String) {
        currentConfig = config
        currentPlayerId = playerId
        currentPlayerName = playerName
        isManualDisconnect = false
        reconnectJob?.cancel()

        _connectionInfo.value = ConnectionInfo(
            status = ConnectionStatus.CONNECTING,
            hostIp = config.host,
            port = config.port,
            roomId = config.roomId
        )

        initiateWebSocket(isReconnect = false)
    }

    private fun initiateWebSocket(isReconnect: Boolean) {
        val config = currentConfig ?: return
        val url = config.websocketUrl

        Log.d(tag, "Connecting to WebSocket at $url (isReconnect: $isReconnect)")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket?.cancel()
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) {
                Log.d(tag, "WebSocket connected successfully!")
                _connectionInfo.value = _connectionInfo.value.copy(
                    status = ConnectionStatus.CONNECTED,
                    errorMessage = null
                )

                // Send initial join or reconnect handshake
                val pId = currentPlayerId ?: "p_${System.currentTimeMillis()}"
                val pName = currentPlayerName ?: "لاعب"
                val rId = config.roomId

                val handshakeMessage = if (isReconnect) {
                    ClientMessageBuilder.buildReconnectMessage(pId, pName, rId)
                } else {
                    ClientMessageBuilder.buildJoinMessage(pId, pName, rId)
                }

                ws.send(handshakeMessage)
            }

            override fun onMessage(ws: WebSocket, text: String) {
                Log.d(tag, "Incoming message: $text")
                val parsed = ServerMessage.parse(text)
                scope.launch {
                    _incomingMessages.emit(parsed)
                }
            }

            override fun onClosing(ws: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closing code=$code reason=$reason")
                ws.close(code, reason)
            }

            override fun onClosed(ws: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closed code=$code reason=$reason")
                if (!isManualDisconnect) {
                    scheduleReconnect()
                } else {
                    _connectionInfo.value = _connectionInfo.value.copy(status = ConnectionStatus.DISCONNECTED)
                }
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket error: ${t.localizedMessage}", t)
                if (!isManualDisconnect) {
                    _connectionInfo.value = _connectionInfo.value.copy(
                        status = ConnectionStatus.RECONNECTING,
                        errorMessage = "انقطع الاتصال، جارٍ إعادة الاتصال..."
                    )
                    scheduleReconnect()
                } else {
                    _connectionInfo.value = _connectionInfo.value.copy(
                        status = ConnectionStatus.ERROR,
                        errorMessage = t.localizedMessage
                    )
                }
            }
        })
    }

    private fun scheduleReconnect() {
        if (isManualDisconnect) return

        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            _connectionInfo.value = _connectionInfo.value.copy(
                status = ConnectionStatus.RECONNECTING,
                errorMessage = "انقطع الاتصال، جارٍ إعادة الاتصال..."
            )

            var attempts = 0
            while (isActive && !isManualDisconnect) {
                attempts++
                val delayTime = (2000L * attempts).coerceAtMost(6000L)
                Log.d(tag, "Attempting reconnect in ${delayTime}ms (Attempt #$attempts)...")
                delay(delayTime)

                if (isActive && !isManualDisconnect) {
                    initiateWebSocket(isReconnect = true)
                    break
                }
            }
        }
    }

    fun sendMessage(jsonString: String): Boolean {
        val ws = webSocket
        if (ws != null && _connectionInfo.value.isConnected) {
            return ws.send(jsonString)
        }
        return false
    }

    fun disconnect() {
        isManualDisconnect = true
        reconnectJob?.cancel()
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionInfo.value = ConnectionInfo(status = ConnectionStatus.DISCONNECTED)
    }
}
