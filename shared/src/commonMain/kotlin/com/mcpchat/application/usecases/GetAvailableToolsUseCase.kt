package com.mcpchat.application.usecases

import com.mcpchat.domain.entities.McpTool
import com.mcpchat.domain.repositories.ChatRepository

/**
 * Use Case for getting available MCP tools
 */
class GetAvailableToolsUseCase(
    private val chatRepository: ChatRepository
) {
    suspend operator fun invoke(): Result<List<McpTool>> {
        return chatRepository.getAvailableTools()
    }
}
