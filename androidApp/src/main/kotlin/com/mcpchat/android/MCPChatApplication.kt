package com.mcpchat.android

import android.app.Application
import com.mcpchat.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

/**
 * Android Application class
 * Initializes Koin dependency injection
 */
class MCPChatApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        
        startKoin {
            androidLogger()
            androidContext(this@MCPChatApplication)
            modules(appModule)
        }
    }
}
