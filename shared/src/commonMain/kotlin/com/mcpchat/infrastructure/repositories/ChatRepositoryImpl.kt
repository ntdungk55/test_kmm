package com.mcpchat.infrastructure.repositories

import com.mcpchat.domain.entities.ChatSession
import com.mcpchat.domain.entities.Message
import com.mcpchat.domain.entities.MessageRole
import com.mcpchat.domain.entities.McpTool
import com.mcpchat.domain.repositories.ChatRepository
import com.mcpchat.infrastructure.ai.ClaudeApiClient
import com.mcpchat.infrastructure.ai.ClaudeMessage
import com.mcpchat.infrastructure.ai.ClaudeTool
import com.mcpchat.infrastructure.mcp.McpClient
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.JsonElement
import kotlin.random.Random

/**
 * Chat Repository Implementation
 * Infrastructure layer - adapter implementing domain repository interface
 */
class ChatRepositoryImpl(
    private val mcpClient: McpClient,
    private val claudeApiClient: ClaudeApiClient
) : ChatRepository {
    
    private val _chatSession = MutableStateFlow(ChatSession(
        id = Random.nextLong().toString(),
        messages = emptyList(),
        availableTools = emptyList(),
        isConnected = false
    ))
    
    override fun getChatSession(): Flow<ChatSession> = _chatSession.asStateFlow()
    
    override suspend fun connect(): Result<Unit> = runCatching {
        mcpClient.connect().getOrThrow()
        
        val tools = mcpClient.requestTools().getOrElse { emptyList() }
        
        _chatSession.update { session ->
            session.copy(
                isConnected = true,
                availableTools = tools
            )
        }
    }
    
    override suspend fun disconnect() {
        mcpClient.disconnect()
        _chatSession.update { it.copy(isConnected = false) }
    }
    
    override suspend fun sendMessage(content: String): Result<Message> = runCatching {
        // Add user message to session
        val userMessage = Message(
            id = Random.nextLong().toString(),
            content = content,
            role = MessageRole.USER
        )
        
        _chatSession.update { session ->
            session.copy(messages = session.messages + userMessage)
        }
        
        // Convert messages to Claude format
        val claudeMessages = _chatSession.value.messages.map { msg ->
            ClaudeMessage(
                role = when (msg.role) {
                    MessageRole.USER -> "user"
                    MessageRole.ASSISTANT -> "assistant"
                    MessageRole.SYSTEM -> "user" // Claude doesn't have system role in messages array
                },
                content = msg.content
            )
        }
        
        // Convert MCP tools to Claude tools format
        val claudeTools = _chatSession.value.availableTools.map { tool ->
            ClaudeTool(
                name = tool.name,
                description = tool.description,
                inputSchema = tool.inputSchema
            )
        }
        
        // Send to Claude
        val response = claudeApiClient.sendMessage(
            messages = claudeMessages,
            tools = claudeTools.ifEmpty { null }
        ).getOrThrow()
        
        // Extract text content from response
        val assistantContent = response.content
            .firstOrNull { it.text != null }
            ?.text ?: "No response"
        
        val assistantMessage = Message(
            id = Random.nextLong().toString(),
            content = assistantContent,
            role = MessageRole.ASSISTANT
        )
        
        // Handle tool calls if any
        response.content.forEach { content ->
            content.toolUse?.let { toolUse ->
                executeTool(toolUse.name, toolUse.input)
            }
        }
        
        _chatSession.update { session ->
            session.copy(messages = session.messages + assistantMessage)
        }
        
        assistantMessage
    }
    
    override suspend fun getAvailableTools(): Result<List<McpTool>> = runCatching {
        mcpClient.requestTools().getOrThrow()
    }
    
    override suspend fun executeTool(
        toolName: String,
        arguments: Map<String, JsonElement>
    ): Result<String> = runCatching {
        mcpClient.callTool(toolName, arguments).getOrThrow()
    }
}
