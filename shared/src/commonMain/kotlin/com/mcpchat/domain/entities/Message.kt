package com.mcpchat.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock


@Serializable
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Long = Clock.System.now().toEpochMilliseconds()
)
@Serializable
enum class MessageRole {
    USER,
    ASSISTANT,
    SYSTEM
}
