package com.example.network

import com.example.model.ServerConfig
import org.json.JSONObject
import java.net.URI

object QrCodeParser {
    fun parse(rawCode: String): ServerConfig? {
        val trimmed = rawCode.trim()
        if (trimmed.isEmpty()) return null

        // 1. Try parsing as JSON: {"ip": "192.168.1.10", "port": 8765, "room": "xyz"}
        if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
            try {
                val json = JSONObject(trimmed)
                val host = json.optString("ip", json.optString("host", "")).trim()
                val port = json.optInt("port", 8765)
                val room = json.optString("room", json.optString("room_id", "default")).trim()
                if (host.isNotEmpty()) {
                    return ServerConfig(host = host, port = port, roomId = room.ifEmpty { "default" })
                }
            } catch (e: Exception) {
                // fall through to url parsing
            }
        }

        // 2. Try parsing as ws:// or http:// or copticquiz:// URI
        try {
            val uriString = if (!trimmed.contains("://")) "ws://$trimmed" else trimmed
            val uri = URI(uriString)
            val host = uri.host
            if (!host.isNullOrEmpty()) {
                val port = if (uri.port != -1) uri.port else 8765
                // Check query parameters for room
                val query = uri.query
                var room = "default"
                if (query != null) {
                    val pairs = query.split("&")
                    for (pair in pairs) {
                        val kv = pair.split("=")
                        if (kv.size == 2 && (kv[0] == "room" || kv[0] == "roomId")) {
                            room = kv[1]
                        }
                    }
                }
                return ServerConfig(host = host, port = port, roomId = room)
            }
        } catch (e: Exception) {
            // fall through
        }

        // 3. Try parsing as simple "IP:PORT" or just "IP"
        val parts = trimmed.replace("ws://", "").replace("http://", "").split(":")
        if (parts.isNotEmpty()) {
            val host = parts[0].trim()
            val port = if (parts.size > 1) parts[1].toIntOrNull() ?: 8765 else 8765
            if (host.isNotEmpty()) {
                return ServerConfig(host = host, port = port, roomId = "default")
            }
        }

        return null
    }
}
