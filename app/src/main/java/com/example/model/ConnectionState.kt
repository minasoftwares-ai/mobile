package com.example.model

enum class ConnectionStatus {
    DISCONNECTED,
    CONNECTING,
    CONNECTED,
    RECONNECTING,
    ERROR
}

data class ConnectionInfo(
    val status: ConnectionStatus = ConnectionStatus.DISCONNECTED,
    val hostIp: String = "",
    val port: Int = 8765,
    val roomId: String = "",
    val errorMessage: String? = null
) {
    val isConnected: Boolean
        get() = status == ConnectionStatus.CONNECTED

    val isReconnecting: Boolean
        get() = status == ConnectionStatus.RECONNECTING
}
