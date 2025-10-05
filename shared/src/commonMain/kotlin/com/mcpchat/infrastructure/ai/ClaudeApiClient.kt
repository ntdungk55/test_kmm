package com.mcpchat.infrastructure.ai

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Claude API Client
 * Infrastructure layer - adapter for Anthropic Claude API
 */
class ClaudeApiClient(
    private val httpClient: HttpClient,
    private val apiKey: String,
    private val apiUrl: String = "https://api.anthropic.com/v1/messages"
) {
    private val json = Json { ignoreUnknownKeys = true }
    
    suspend fun sendMessage(
        messages: List<ClaudeMessage>,
        model: String = "claude-3-5-sonnet-20241022",
        maxTokens: Int = 1024,
        tools: List<ClaudeTool>? = null
    ): Result<ClaudeResponse> = runCatching {
        val requestBody = ClaudeRequest(
            model = model,
            maxTokens = maxTokens,
            messages = messages,
            tools = tools
        )
        
        val response: HttpResponse = httpClient.post(apiUrl) {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        
        val responseText = response.bodyAsText()
        json.decodeFromString(ClaudeResponse.serializer(), responseText)
    }
    
    suspend fun sendStreamingMessage(
        messages: List<ClaudeMessage>,
        model: String = "claude-3-5-sonnet-20241022",
        maxTokens: Int = 1024,
        onChunk: (String) -> Unit
    ): Result<Unit> = runCatching {
        // Streaming implementation - simplified version
        val requestBody = ClaudeRequest(
            model = model,
            maxTokens = maxTokens,
            messages = messages,
            stream = true
        )
        
        httpClient.post(apiUrl) {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }
        // Handle streaming response chunks
    }
}

@Serializable
data class ClaudeRequest(
    val model: String,
    val maxTokens: Int,
    val messages: List<ClaudeMessage>,
    val tools: List<ClaudeTool>? = null,
    val stream: Boolean = false
)

@Serializable
data class ClaudeMessage(
    val role: String,
    val content: String
)

@Serializable
data class ClaudeTool(
    val name: String,
    val description: String,
    val inputSchema: Map<String, JsonElement>
)

@Serializable
data class ClaudeResponse(
    val id: String,
    val type: String = "message",
    val role: String,
    val content: List<ClaudeContent>,
    val model: String,
    val stopReason: String? = null
)

@Serializable
data class ClaudeContent(
    val type: String,
    val text: String? = null,
    val toolUse: ClaudeToolUse? = null
)

@Serializable
data class ClaudeToolUse(
    val id: String,
    val name: String,
    val input: Map<String, JsonElement>
)
