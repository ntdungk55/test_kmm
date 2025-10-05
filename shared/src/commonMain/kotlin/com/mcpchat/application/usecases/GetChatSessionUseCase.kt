package com.mcpchat.application.usecases

import com.mcpchat.domain.entities.ChatSession
import com.mcpchat.domain.repositories.ChatRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use Case for getting chat session updates
 */
class GetChatSessionUseCase(
    private val chatRepository: ChatRepository
) {
    operator fun invoke(): Flow<ChatSession> {
        return chatRepository.getChatSession()
    }
}
