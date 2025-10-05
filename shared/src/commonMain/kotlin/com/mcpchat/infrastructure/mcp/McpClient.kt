package com.mcpchat.infrastructure.mcp

import com.mcpchat.domain.entities.McpTool
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * MCP (Model Context Protocol) Client Implementation
 * Infrastructure layer - adapter for MCP server communication
 */
class McpClient(
    private val httpClient: HttpClient,
    private val serverUrl: String
) {
    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val _isConnected = MutableStateFlow(false)
    val isConnected: Flow<Boolean> = _isConnected.asStateFlow()
    
    private val _availableTools = MutableStateFlow<List<McpTool>>(emptyList())
    val availableTools: Flow<List<McpTool>> = _availableTools.asStateFlow()
    
    suspend fun connect(): Result<Unit> = runCatching {
        webSocketSession = httpClient.webSocketSession(serverUrl)
        _isConnected.value = true
        
        // Send initialize request
        sendMessage(McpRequest.Initialize(
            protocolVersion = "1.0.0",
            capabilities = mapOf("tools" to true)
        ))
        
        // Request available tools
        requestTools()
    }
    
    suspend fun disconnect() {
        webSocketSession?.close()
        webSocketSession = null
        _isConnected.value = false
    }
    
    suspend fun requestTools(): Result<List<McpTool>> = runCatching {
        val request = McpRequest.ListTools()
        sendMessage(request)
        
        // Wait for response (simplified - in production use proper message correlation)
        val response = receiveMessage()
        val tools = parseToolsFromResponse(response)
        _availableTools.value = tools
        tools
    }
    
    suspend fun callTool(toolName: String, arguments: Map<String, JsonElement>): Result<String> = runCatching {
        val request = McpRequest.CallTool(
            name = toolName,
            arguments = arguments
        )
        sendMessage(request)
        
        val response = receiveMessage()
        parseToolResultFromResponse(response)
    }
    
    private suspend fun sendMessage(request: McpRequest) {
        val json = Json.encodeToString(McpRequest.serializer(), request)
        webSocketSession?.send(Frame.Text(json))
    }
    
    private suspend fun receiveMessage(): String {
        val frame = webSocketSession?.incoming?.receive() as? Frame.Text
        return frame?.readText() ?: ""
    }
    
    private fun parseToolsFromResponse(response: String): List<McpTool> {
        return try {
            val jsonElement = Json.parseToJsonElement(response)
            val toolsArray = jsonElement.jsonObject["tools"]?.jsonArray ?: return emptyList()
            
            toolsArray.map { toolElement ->
                val tool = toolElement.jsonObject
                McpTool(
                    name = tool["name"]?.jsonPrimitive?.content ?: "",
                    description = tool["description"]?.jsonPrimitive?.content ?: "",
                    inputSchema = tool["inputSchema"]?.jsonObject?.toMap() ?: emptyMap()
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun parseToolResultFromResponse(response: String): String {
        return try {
            val jsonElement = Json.parseToJsonElement(response)
            jsonElement.jsonObject["result"]?.jsonPrimitive?.content ?: ""
        } catch (e: Exception) {
            "Error parsing tool result"
        }
    }
}

@Serializable
sealed class McpRequest {
    @Serializable
    data class Initialize(
        val protocolVersion: String,
        val capabilities: Map<String, Boolean>
    ) : McpRequest()
    
    @Serializable
    data class ListTools(
        val params: Map<String, String> = emptyMap()
    ) : McpRequest()
    
    @Serializable
    data class CallTool(
        val name: String,
        val arguments: Map<String, JsonElement>
    ) : McpRequest()
}
