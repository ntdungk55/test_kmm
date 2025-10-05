package com.mcpchat.application.usecases

import com.mcpchat.domain.repositories.ChatRepository

/**
 * Use Case for connecting to MCP server
 */
class ConnectToMcpUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(): Result<Unit> {
        return chatRepository.connect()
    }
}
