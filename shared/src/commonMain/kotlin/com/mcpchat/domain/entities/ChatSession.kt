package com.mcpchat.domain.entities

import kotlinx.serialization.Serializable

@Serializable
data class ChatSession(
    val id: String,
    val messages: List<Message> = emptyList(),
    val availableTools: List<McpTool> = emptyList(),
    val isConnected: Boolean = false
)
