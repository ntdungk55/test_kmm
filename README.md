# MCP Chat App - Kotlin Multiplatform Mobile

Ứng dụng chat đa nền tảng (Android & iOS) kết nối với Claude AI thông qua MCP (Model Context Protocol) server, được xây dựng bằng Kotlin Multiplatform và Jetpack Compose.

## 🏗️ Kiến trúc

Dự án này tuân theo **Clean Architecture** với các lớp rõ ràng:

```
📦 MCPChatApp
├── 📂 shared (Code chung cho Android & iOS)
│   ├── 📂 domain (Business logic thuần túy)
│   │   ├── 📂 entities (Message, ChatSession, McpTool)
│   │   └── 📂 repositories (ChatRepository interface)
│   ├── 📂 application (Use cases)
│   │   └── 📂 usecases (SendMessageUseCase, ConnectToMcpUseCase, ...)
│   ├── 📂 infrastructure (Implementations)
│   │   ├── 📂 mcp (McpClient - WebSocket connection)
│   │   ├── 📂 ai (ClaudeApiClient - HTTP REST API)
│   │   └── 📂 repositories (ChatRepositoryImpl)
│   ├── 📂 presentation (UI Layer)
│   │   └── 📂 screens/chat (ChatScreen, ChatViewModel)
│   └── 📂 di (Dependency Injection với Koin)
├── 📂 androidApp (Android app)
└── 📂 iosApp (iOS app)
```

## 🎨 Tính năng

- ✅ **Chat với Claude AI** - Giao diện chat hiện đại và mượt mà
- ✅ **MCP Integration** - Kết nối với MCP server để sử dụng các tools
- ✅ **Clean Architecture** - Code dễ maintain, test và mở rộng
- ✅ **Compose Multiplatform** - UI code chung cho cả Android và iOS
- ✅ **Real-time Updates** - Flow/StateFlow cho reactive UI
- ✅ **Dependency Injection** - Koin cho DI đơn giản và hiệu quả
- ✅ **Material Design 3** - UI đẹp mắt với Material 3

## 🛠️ Tech Stack

### Shared Code
- **Kotlin Multiplatform** - Code sharing giữa platforms
- **Compose Multiplatform** - UI framework
- **Ktor Client** - HTTP & WebSocket client
- **Kotlinx.serialization** - JSON serialization
- **Kotlinx.coroutines** - Async programming
- **Koin** - Dependency injection
- **Voyager** - Navigation library

### Android
- **Jetpack Compose** - Modern UI toolkit
- **Material 3** - Design system

### iOS
- **SwiftUI** - iOS UI wrapper
- **UIKit Integration** - Bridge to Compose

## 📋 Yêu cầu

- **Android Studio** Hedgehog (2023.1.1) hoặc mới hơn
- **Xcode** 15+ (cho iOS development)
- **JDK** 11 hoặc mới hơn
- **Claude API Key** từ [Anthropic](https://www.anthropic.com)
- **MCP Server** đang chạy (hoặc sử dụng mock server)

## 🚀 Cài đặt

### 1. Clone repository

```bash
git clone <your-repo-url>
cd MCPChatApp
```

### 2. Cấu hình Claude API Key

Mở file `shared/src/commonMain/kotlin/com/mcpchat/di/AppModule.kt` và thêm API key:

```kotlin
single {
    ClaudeApiClient(
        httpClient = get(),
        apiKey = "YOUR_CLAUDE_API_KEY_HERE" // Thay đổi ở đây
    )
}
```

### 3. Cấu hình MCP Server URL

Trong cùng file, cập nhật URL của MCP server:

```kotlin
single {
    McpClient(
        httpClient = get(),
        serverUrl = "ws://your-mcp-server:3000/mcp" // Thay đổi ở đây
    )
}
```

### 4. Build & Run

#### Android
```bash
./gradlew :androidApp:installDebug
```

Hoặc mở project trong Android Studio và chạy configuration `androidApp`.

#### iOS
```bash
cd iosApp
pod install  # Nếu cần
open iosApp.xcworkspace
```

Hoặc mở `iosApp.xcworkspace` trong Xcode và run.

## 📱 Sử dụng

1. **Khởi động app** - App sẽ tự động kết nối với MCP server
2. **Chờ kết nối** - Status bar sẽ hiển thị "Connected" khi kết nối thành công
3. **Nhập tin nhắn** - Gõ tin nhắn vào ô input ở dưới cùng
4. **Gửi** - Nhấn nút "Send" để gửi tin nhắn cho Claude AI
5. **Nhận phản hồi** - Claude sẽ trả lời và có thể sử dụng MCP tools nếu cần

## 🧪 Testing

### Unit Tests
```bash
./gradlew :shared:testDebugUnitTest
```

### Android Instrumented Tests
```bash
./gradlew :androidApp:connectedAndroidTest
```

## 🏛️ Clean Architecture Layers

### 1. Domain Layer (Entities & Repositories)
- **Entities**: `Message`, `ChatSession`, `McpTool`
- **Repositories**: `ChatRepository` interface
- Không phụ thuộc vào bất kỳ layer nào khác
- Pure Kotlin, không có Android/iOS dependencies

### 2. Application Layer (Use Cases)
- `SendMessageUseCase` - Gửi tin nhắn
- `ConnectToMcpUseCase` - Kết nối MCP
- `GetChatSessionUseCase` - Lấy chat session
- `GetAvailableToolsUseCase` - Lấy danh sách tools
- Orchestrates business logic
- Phụ thuộc vào Domain layer

### 3. Infrastructure Layer (Adapters & Implementations)
- `McpClient` - WebSocket client cho MCP protocol
- `ClaudeApiClient` - HTTP client cho Claude API
- `ChatRepositoryImpl` - Implementation của ChatRepository
- Implements ports định nghĩa trong Domain
- Phụ thuộc vào Domain & Application layers

### 4. Presentation Layer (UI & ViewModels)
- `ChatScreen` - Compose UI
- `ChatViewModel` - State management
- Phụ thuộc vào Application layer (use cases)
- Không biết về Infrastructure implementations

### 5. DI Layer (Dependency Injection)
- `AppModule` - Koin modules
- Wires everything together
- Dependencies flow inward (Infrastructure → Application → Domain)

## 🔌 MCP Protocol

App này implement MCP (Model Context Protocol) để:
- Kết nối với MCP server qua WebSocket
- Discover available tools từ server
- Execute tools và nhận results
- Tích hợp tool results vào conversation với Claude

### MCP Message Format

```json
{
  "type": "request",
  "method": "tools/list",
  "params": {}
}
```

## 🎯 Best Practices

1. **Separation of Concerns** - Mỗi layer có trách nhiệm riêng
2. **Dependency Inversion** - High-level modules không phụ thuộc low-level
3. **Single Responsibility** - Mỗi class có một lý do duy nhất để thay đổi
4. **Testability** - Dễ dàng mock và test các components
5. **Reactive Programming** - Sử dụng Flow cho real-time updates
6. **Error Handling** - Result type cho error handling rõ ràng

## 📚 Tài liệu tham khảo

- [Kotlin Multiplatform](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)
- [Ktor](https://ktor.io/)
- [Koin](https://insert-koin.io/)
- [Claude API](https://docs.anthropic.com/claude/reference/)
- [MCP Protocol](https://modelcontextprotocol.io/)

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

MIT License - feel free to use this project for learning or production.

## 👨‍💻 Author

Built with ❤️ using Kotlin Multiplatform & Compose

---

**Note**: Đây là ví dụ educational. Trong production, bạn nên:
- Lưu API key an toàn (không hardcode)
- Implement proper error handling
- Add retry logic
- Implement caching
- Add analytics
- Implement proper security measures
- Add comprehensive tests
- Handle edge cases better
