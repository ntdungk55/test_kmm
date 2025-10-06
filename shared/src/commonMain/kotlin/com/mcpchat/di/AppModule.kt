package com.mcpchat.di

import com.mcpchat.application.usecases.*
import com.mcpchat.domain.repositories.ChatRepository
import com.mcpchat.infrastructure.ai.ClaudeApiClient
import com.mcpchat.infrastructure.mcp.McpClient
import com.mcpchat.infrastructure.repositories.ChatRepositoryImpl
import com.mcpchat.presentation.screens.chat.ChatViewModel
import io.ktor.client.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import mcpchatapp.shared.BuildKonfig
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.module

/**
 * Dependency Injection Module using Koin
 * Following Clean Architecture - dependencies flow inward
 */
val appModule = module {
    // HTTP Client
    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                })
            }
            install(Logging) {
                level = LogLevel.INFO
            }
            install(WebSockets)
        }
    }
    
    // Infrastructure Layer
    single {
        McpClient(
            httpClient = get(),
            serverUrl = BuildKonfig.MCP_SERVER_URL // Configure your MCP server URL
        )
    }
    
    single {
        ClaudeApiClient(
            httpClient = get(),
            apiKey = BuildKonfig.CLAUDE_API_KEY // Add your Claude API key here or from config
        )
    }
    
    // Repository Implementation (Infrastructure -> Domain)
    single<ChatRepository> {
        ChatRepositoryImpl(
            mcpClient = get(),
            claudeApiClient = get()
        )
    }
    
    // Use Cases (Application Layer)
    factoryOf(::SendMessageUseCase)
    factoryOf(::ConnectToMcpUseCase)
    factoryOf(::GetChatSessionUseCase)
    factoryOf(::GetAvailableToolsUseCase)
    
    // ViewModels (Presentation Layer)
    factoryOf(::ChatViewModel)
}
