package com.mcpchat

import androidx.compose.ui.window.ComposeUIViewController
import cafe.adriel.voyager.navigator.Navigator
import com.mcpchat.presentation.screens.chat.ChatScreen
import platform.UIKit.UIViewController

fun createChatViewController(): UIViewController {
    return ComposeUIViewController {
        Navigator(ChatScreen())
    }
}
