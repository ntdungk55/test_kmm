package com.mcpchat.domain.repositories

import com.mcpchat.domain.entities.ChatSession
import com.mcpchat.domain.entities.Message
import com.mcpchat.domain.entities.McpTool
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.JsonElement

/**
 * Repository interface for chat operations
 * Following Clean Architecture principles - this is a port in the domain layer
 */
interface ChatRepository {
    /**
     * Get chat session as a Flow for real-time updates
     */
    fun getChatSession(): Flow<ChatSession>
    
    /**
     * Send a message and get response from Claude AI
     */
    suspend fun sendMessage(content: String): Result<Message>
    
    /**
     * Connect to MCP server
     */
    suspend fun connect(): Result<Unit>
    
    /**
     * Disconnect from MCP server
     */
    suspend fun disconnect()
    
    /**
     * Get available MCP tools
     */
    suspend fun getAvailableTools(): Result<List<McpTool>>
    
    /**
     * Execute an MCP tool
     */
    suspend fun executeTool(toolName: String, arguments: Map<String, JsonElement>): Result<String>
}
