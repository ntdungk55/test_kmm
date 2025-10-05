package com.mcpchat.presentation.screens.chat

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.navigator.Navigator

@Composable
fun AppRoot() {
    Navigator(ChatScreen())
}