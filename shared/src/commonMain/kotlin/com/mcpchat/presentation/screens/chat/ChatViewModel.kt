package com.mcpchat.presentation.screens.chat

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.mcpchat.application.usecases.*
import com.mcpchat.domain.entities.ChatSession
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Chat ViewModel
 * Presentation layer - manages UI state and user interactions
 */
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val connectToMcpUseCase: ConnectToMcpUseCase,
    private val getChatSessionUseCase: GetChatSessionUseCase,
    private val getAvailableToolsUseCase: GetAvailableToolsUseCase
) : ScreenModel {
    
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    
    init {
        observeChatSession()
        connectToMcp()
    }
    
    private fun observeChatSession() {
        screenModelScope.launch {
            getChatSessionUseCase()
                .catch { error ->
                    _uiState.update { 
                        it.copy(
                            error = error.message ?: "Unknown error",
                            isLoading = false
                        )
                    }
                }
                .collect { session ->
                    _uiState.update {
                        it.copy(
                            chatSession = session,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }
    
    private fun connectToMcp() {
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            connectToMcpUseCase()
                .onSuccess {
                    loadAvailableTools()
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = "Failed to connect: ${error.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    private fun loadAvailableTools() {
        screenModelScope.launch {
            getAvailableToolsUseCase()
                .onSuccess { tools ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = "Failed to load tools: ${error.message}",
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun sendMessage(content: String) {
        if (content.isBlank()) return
        
        screenModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            sendMessageUseCase(content)
                .onSuccess {
                    _uiState.update { 
                        it.copy(
                            isLoading = false,
                            error = null
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            error = error.message ?: "Failed to send message",
                            isLoading = false
                        )
                    }
                }
        }
    }
    
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class ChatUiState(
    val chatSession: ChatSession = ChatSession(
        id = "",
        messages = emptyList(),
        availableTools = emptyList(),
        isConnected = false
    ),
    val isLoading: Boolean = false,
    val error: String? = null
)
