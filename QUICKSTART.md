# Quick Start Guide

## 🚀 Bắt đầu nhanh trong 5 phút

### Bước 1: Cài đặt dependencies

Đảm bảo bạn đã cài:
- ✅ Android Studio Hedgehog hoặc mới hơn
- ✅ JDK 11+
- ✅ Xcode 15+ (chỉ dành cho iOS development trên Mac)

### Bước 2: Clone và mở project

```bash
git clone <your-repo-url>
cd MCPChatApp
```

Mở project trong Android Studio:
```bash
open -a "Android Studio" .
```

### Bước 3: Cấu hình API Key

#### Option 1: Hardcode (Chỉ cho development)

Mở `shared/src/commonMain/kotlin/com/mcpchat/di/AppModule.kt`:

```kotlin
single {
    ClaudeApiClient(
        httpClient = get(),
        apiKey = "sk-ant-api03-..." // Paste your Claude API key here
    )
}
```

#### Option 2: Sử dụng local.properties (Recommended)

1. Copy file example:
```bash
cp local.properties.example local.properties
```

2. Edit `local.properties`:
```properties
claude.api.key=sk-ant-api03-your-key-here
mcp.server.url=ws://localhost:3000/mcp
```

3. Load trong build.gradle.kts (đã cấu hình sẵn)

### Bước 4: Cấu hình MCP Server

#### Option 1: Chạy MCP Server thực

Nếu bạn có MCP server, cập nhật URL trong `AppModule.kt`:

```kotlin
single {
    McpClient(
        httpClient = get(),
        serverUrl = "ws://your-server:3000/mcp"
    )
}
```

#### Option 2: Mock Server (Development)

Tạo một mock server đơn giản bằng Node.js:

```javascript
// mock-mcp-server.js
const WebSocket = require('ws');
const wss = new WebSocket.Server({ port: 3000, path: '/mcp' });

wss.on('connection', (ws) => {
  console.log('Client connected');
  
  ws.on('message', (message) => {
    console.log('Received:', message);
    
    // Mock response
    ws.send(JSON.stringify({
      tools: [
        {
          name: "calculator",
          description: "Perform calculations",
          inputSchema: { type: "object" }
        }
      ]
    }));
  });
});

console.log('Mock MCP server running on ws://localhost:3000/mcp');
```

Chạy:
```bash
npm install ws
node mock-mcp-server.js
```

### Bước 5: Run Android App

#### Từ Android Studio:
1. Chọn `androidApp` configuration
2. Chọn device/emulator
3. Click Run ▶️

#### Từ terminal:
```bash
./gradlew :androidApp:installDebug
```

### Bước 6: Run iOS App (Chỉ trên Mac)

1. Mở terminal tại root project
2. Build shared framework:
```bash
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

3. Mở Xcode project:
```bash
cd iosApp
open iosApp.xcodeproj
```

4. Chọn simulator và click Run ▶️

## 🎮 Sử dụng App

### Android/iOS

1. **Mở app** - App tự động kết nối với MCP server
2. **Chờ kết nối** - Status bar hiển thị "Connected" với số tools
3. **Chat** - Gõ tin nhắn: "Hello Claude!"
4. **Nhận response** - Claude trả lời ngay lập tức

### Test Messages

Thử các messages sau:

```
"Hello! What can you do?"
"What tools do you have access to?"
"Can you help me with X?"
```

## 🐛 Troubleshooting

### Lỗi: "Failed to connect to MCP server"

**Giải pháp:**
- Kiểm tra MCP server đang chạy
- Verify URL trong `AppModule.kt`
- Kiểm tra firewall settings
- Android emulator: Sử dụng `10.0.2.2` thay vì `localhost`

```kotlin
serverUrl = "ws://10.0.2.2:3000/mcp" // For Android emulator
```

### Lỗi: "Claude API authentication failed"

**Giải pháp:**
- Verify API key correct
- Kiểm tra API key còn active
- Test API key với curl:

```bash
curl https://api.anthropic.com/v1/messages \
  -H "x-api-key: $YOUR_API_KEY" \
  -H "anthropic-version: 2023-06-01" \
  -H "content-type: application/json" \
  -d '{
    "model": "claude-3-5-sonnet-20241022",
    "max_tokens": 1024,
    "messages": [{"role": "user", "content": "Hello"}]
  }'
```

### Lỗi: Build failed - "Cannot find ktor"

**Giải pháp:**
```bash
./gradlew clean
./gradlew build --refresh-dependencies
```

### iOS: "Framework not found"

**Giải pháp:**
```bash
./gradlew :shared:clean
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

### Android: "Cleartext HTTP traffic not permitted"

**Giải pháp:**
- Đã được config trong `AndroidManifest.xml`:
```xml
android:usesCleartextTraffic="true"
```

## 📱 Demo Mode (Không cần API key)

Để test UI mà không cần API key hoặc MCP server:

1. Mở `ChatRepositoryImpl.kt`
2. Comment out API calls
3. Return mock data:

```kotlin
override suspend fun sendMessage(content: String): Result<Message> = runCatching {
    // Mock response
    delay(1000)
    val mockResponse = Message(
        id = UUID.randomUUID().toString(),
        content = "This is a mock response. Set up Claude API key for real responses!",
        role = MessageRole.ASSISTANT
    )
    
    _chatSession.update { session ->
        session.copy(messages = session.messages + mockResponse)
    }
    
    mockResponse
}
```

## 🎯 Next Steps

1. **Customize UI** - Edit `ChatScreen.kt` để thay đổi giao diện
2. **Add Features** - Implement voice input, image upload, etc.
3. **Add Tools** - Create custom MCP tools
4. **Production Setup** - Secure API keys, add analytics, error tracking

## 📚 Học thêm

- [Full README](README.md) - Chi tiết về architecture và features
- [ARCHITECTURE.md](ARCHITECTURE.md) - Deep dive vào Clean Architecture
- [Kotlin Multiplatform Docs](https://kotlinlang.org/docs/multiplatform.html)
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/)

## ❓ Có câu hỏi?

Mở issue trên GitHub hoặc check documentation!

Happy coding! 🚀
