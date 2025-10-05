package com.mcpchat.application.usecases

import com.mcpchat.domain.entities.Message
import com.mcpchat.domain.repositories.ChatRepository

/**
 * Use Case for sending a message to Claude AI
 * Application layer - orchestrates business logic
 */
class SendMessageUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(content: String): Result<Message> {
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        
        return chatRepository.sendMessage(content)
    }
}
