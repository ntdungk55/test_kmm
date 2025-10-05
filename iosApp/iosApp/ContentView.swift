import SwiftUI
import Shared

struct ContentView: View {
    var body: some View {
        ChatScreenWrapper()
    }
}

struct ChatScreenWrapper: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        return ChatScreenKt.createChatViewController()
    }
    
    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        // Update logic if needed
    }
}
