package com.mcpchat.domain.entities

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class McpTool(
    val name: String,
    val description: String,
    val inputSchema: Map<String, JsonElement>
)

@Serializable
data class McpToolCall(
    val toolName: String,
    val arguments: Map<String, JsonElement>
)

@Serializable
data class McpToolResult(
    val toolName: String,
    val result: String,
    val isError: Boolean = false
)
