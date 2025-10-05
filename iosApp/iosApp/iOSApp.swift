import SwiftUI
import Shared

@main
struct iOSApp: App {
    
    init() {
        // Initialize Koin for iOS
        KoinInitializerKt.doInitKoin()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
