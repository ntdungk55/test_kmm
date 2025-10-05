package com.mcpchat.presentation.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import com.mcpchat.domain.entities.Message
import com.mcpchat.domain.entities.MessageRole
import kotlinx.coroutines.launch

/**
 * Chat Screen with Compose Multiplatform
 * Beautiful and modern UI for chat with Claude AI
 */
class ChatScreen : Screen {
    @Composable
    override fun Content() {
        val viewModel = getScreenModel<ChatViewModel>()
        val uiState by viewModel.uiState.collectAsState()
        
        ChatScreenContent(
            uiState = uiState,
            onSendMessage = viewModel::sendMessage,
            onClearError = viewModel::clearError
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreenContent(
    uiState: ChatUiState,
    onSendMessage: (String) -> Unit,
    onClearError: () -> Unit
) {
    var messageText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    
    // Auto scroll to bottom when new messages arrive
    LaunchedEffect(uiState.chatSession.messages.size) {
        if (uiState.chatSession.messages.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(uiState.chatSession.messages.size - 1)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("MCP Chat with Claude", fontWeight = FontWeight.Bold)
                        Text(
                            text = if (uiState.chatSession.isConnected) 
                                "Connected • ${uiState.chatSession.availableTools.size} tools" 
                            else "Disconnected",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (uiState.chatSession.isConnected) 
                                Color.Green 
                            else Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Messages List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                state = listState,
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.chatSession.messages) { message ->
                    MessageBubble(message)
                }
                
                if (uiState.isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }
            
            // Error Message
            if (uiState.error != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = uiState.error,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = onClearError) {
                            Text("Dismiss")
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Input Field
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Type a message...") },
                        shape = RoundedCornerShape(24.dp),
                        enabled = !uiState.isLoading && uiState.chatSession.isConnected
                    )
                    val canInteract = !uiState.isLoading && uiState.chatSession.isConnected
                    val isSendButtonEnabled = canInteract && messageText.isNotBlank()
                    FloatingActionButton(
                        onClick = {
                            // 1. CHẶN HÀNH ĐỘNG
                            if (isSendButtonEnabled) {
                                onSendMessage(messageText)
                                messageText = ""
                            }
                        },
                        // 2. PHẢN HỒI TRỰC QUAN
                        containerColor = if (isSendButtonEnabled)
                            FloatingActionButtonDefaults.containerColor
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                        contentColor = if (isSendButtonEnabled)
                            contentColorFor(FloatingActionButtonDefaults.containerColor)
                        else
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                    ) {
                        Text("Send", modifier = Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun MessageBubble(message: Message) {
    val isUser = message.role == MessageRole.USER
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            shape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 16.dp
            ),
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) 
                    MaterialTheme.colorScheme.primaryContainer
                else 
                    MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isUser)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Card(
            modifier = Modifier.widthIn(max = 100.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }
        }
    }
}
