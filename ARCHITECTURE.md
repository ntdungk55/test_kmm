# Architecture Documentation

## 🏛️ Clean Architecture Overview

Dự án này implement Clean Architecture với Kotlin Multiplatform, đảm bảo code separation, testability và maintainability.

## Dependency Rule

```
Presentation Layer (UI)
       ↓
Application Layer (Use Cases)
       ↓
Domain Layer (Business Logic)
       ↑
Infrastructure Layer (Implementations)
```

**Rule**: Dependencies chỉ flow inward. Inner layers không biết về outer layers.

## Layer Details

### 1. Domain Layer 🎯

**Location**: `shared/src/commonMain/kotlin/com/mcpchat/domain/`

**Responsibility**: Chứa business logic thuần túy, platform-agnostic

**Components**:
- **Entities**: Business objects
  - `Message`: Chat message entity
  - `ChatSession`: Chat session state
  - `McpTool`: MCP tool representation
  
- **Repository Interfaces**: Contracts (ports)
  - `ChatRepository`: Chat operations contract

**Rules**:
- ✅ Pure Kotlin code only
- ✅ No external dependencies
- ✅ No framework dependencies
- ❌ No UI code
- ❌ No database code
- ❌ No network code

**Example**:
```kotlin
// Good - Pure domain entity
data class Message(
    val id: String,
    val content: String,
    val role: MessageRole
)

// Bad - Contains framework dependency
data class Message(
    val id: String,
    val content: String,
    @SerializedName("role") val role: MessageRole // ❌ Framework annotation
)
```

### 2. Application Layer 🔄

**Location**: `shared/src/commonMain/kotlin/com/mcpchat/application/`

**Responsibility**: Orchestrates business logic, coordinates between layers

**Components**:
- **Use Cases**: Single-purpose application operations
  - `SendMessageUseCase`: Handles sending messages
  - `ConnectToMcpUseCase`: Manages MCP connection
  - `GetChatSessionUseCase`: Retrieves chat session
  - `GetAvailableToolsUseCase`: Gets available tools

**Rules**:
- ✅ Depends on Domain layer only
- ✅ Contains application-specific business rules
- ✅ One use case = one operation
- ❌ No UI logic
- ❌ No direct external dependencies

**Example**:
```kotlin
class SendMessageUseCase(
    private val chatRepository: ChatRepository // Domain interface
) {
    suspend operator fun invoke(content: String): Result<Message> {
        // Validation (application rule)
        if (content.isBlank()) {
            return Result.failure(IllegalArgumentException("Message cannot be empty"))
        }
        
        // Delegate to repository
        return chatRepository.sendMessage(content)
    }
}
```

### 3. Infrastructure Layer 🔧

**Location**: `shared/src/commonMain/kotlin/com/mcpchat/infrastructure/`

**Responsibility**: Technical implementations, adapters to external systems

**Components**:
- **MCP Client** (`McpClient`): WebSocket adapter for MCP protocol
- **Claude API Client** (`ClaudeApiClient`): HTTP adapter for Claude API
- **Repository Implementation** (`ChatRepositoryImpl`): Implements domain contracts

**Rules**:
- ✅ Implements domain interfaces (adapters)
- ✅ Handles external dependencies (network, DB, etc.)
- ✅ Converts between domain entities and external formats
- ❌ No business logic
- ❌ No UI code

**Example**:
```kotlin
class ChatRepositoryImpl(
    private val mcpClient: McpClient,
    private val claudeApiClient: ClaudeApiClient
) : ChatRepository { // Implements domain interface
    
    override suspend fun sendMessage(content: String): Result<Message> {
        // Technical implementation
        // Converts domain entities ↔ API formats
    }
}
```

### 4. Presentation Layer 🎨

**Location**: `shared/src/commonMain/kotlin/com/mcpchat/presentation/`

**Responsibility**: UI and user interaction

**Components**:
- **Screens** (`ChatScreen`): Compose UI components
- **ViewModels** (`ChatViewModel`): State management and UI logic

**Rules**:
- ✅ Depends on Application layer (use cases)
- ✅ Manages UI state
- ✅ Handles user input
- ❌ No direct repository access
- ❌ No business logic

**Example**:
```kotlin
class ChatViewModel(
    private val sendMessageUseCase: SendMessageUseCase,
    private val getChatSessionUseCase: GetChatSessionUseCase
) : ScreenModel {
    
    fun sendMessage(content: String) {
        screenModelScope.launch {
            // Uses use case, not repository directly
            sendMessageUseCase(content)
        }
    }
}
```

### 5. DI Layer 🔌

**Location**: `shared/src/commonMain/kotlin/com/mcpchat/di/`

**Responsibility**: Dependency injection configuration

**Components**:
- **App Module** (`AppModule`): Koin DI setup

**Wiring**:
```kotlin
val appModule = module {
    // Infrastructure
    single { HttpClient() }
    single { McpClient(get(), "ws://...") }
    single { ClaudeApiClient(get(), "api-key") }
    
    // Repository (Infrastructure implements Domain interface)
    single<ChatRepository> { ChatRepositoryImpl(get(), get()) }
    
    // Use Cases (Application depends on Domain)
    factory { SendMessageUseCase(get()) }
    
    // ViewModels (Presentation depends on Application)
    factory { ChatViewModel(get(), get()) }
}
```

## Data Flow

### User sends a message:

```
1. User types in ChatScreen (Presentation)
        ↓
2. ChatViewModel.sendMessage() (Presentation)
        ↓
3. SendMessageUseCase.invoke() (Application)
        ↓
4. ChatRepository interface (Domain contract)
        ↓
5. ChatRepositoryImpl.sendMessage() (Infrastructure)
        ↓
6. ClaudeApiClient + McpClient (Infrastructure)
        ↓
7. External APIs (Claude API, MCP Server)
```

### Response flows back:

```
1. API Response (Infrastructure)
        ↓
2. ChatRepositoryImpl converts to domain entities
        ↓
3. Result<Message> returns through layers
        ↓
4. ChatViewModel updates state
        ↓
5. ChatScreen re-renders with new data
```

## Benefits of This Architecture

### ✅ Testability
- Each layer can be tested independently
- Easy to mock dependencies
- Domain logic is pure and easy to test

### ✅ Maintainability
- Changes in one layer don't affect others
- Easy to find and fix bugs
- Clear responsibility boundaries

### ✅ Flexibility
- Easy to swap implementations
- Can change UI framework without touching business logic
- Can change database/API without affecting domain

### ✅ Scalability
- Easy to add new features
- Can split into modules easily
- Team members can work on different layers independently

### ✅ Platform Independence
- Business logic is shared between Android and iOS
- Only platform-specific code is in platform modules
- Maximum code reuse

## Testing Strategy

### Domain Layer
```kotlin
@Test
fun `message content cannot be empty`() {
    val message = Message("", "", MessageRole.USER)
    // Test business rules
}
```

### Application Layer
```kotlin
@Test
fun `SendMessageUseCase returns error for empty message`() {
    val useCase = SendMessageUseCase(mockRepository)
    val result = runBlocking { useCase("") }
    assertTrue(result.isFailure)
}
```

### Infrastructure Layer
```kotlin
@Test
fun `ChatRepositoryImpl calls Claude API correctly`() {
    val mockClient = mockk<ClaudeApiClient>()
    val repository = ChatRepositoryImpl(mockMcp, mockClient)
    // Test API integration
}
```

### Presentation Layer
```kotlin
@Test
fun `ChatViewModel updates state when message sent`() {
    val viewModel = ChatViewModel(mockUseCase)
    viewModel.sendMessage("Hello")
    // Assert state changes
}
```

## Common Patterns

### Repository Pattern
- Interface in Domain
- Implementation in Infrastructure
- Abstracts data sources

### Use Case Pattern
- Single Responsibility Principle
- Reusable business operations
- Easy to test

### Observer Pattern (Flow)
- Reactive data streams
- UI automatically updates
- Decoupled communication

### Dependency Injection
- Loose coupling
- Easy to replace implementations
- Better testability

## Best Practices

1. **Keep Domain Pure** - No framework dependencies
2. **Use Interfaces** - Program to contracts, not implementations
3. **Single Responsibility** - One class, one job
4. **Dependency Inversion** - Depend on abstractions
5. **Testability First** - Design for testing
6. **Immutable Entities** - Use `data class` with `val`
7. **Error Handling** - Use `Result<T>` for operations that can fail
8. **Async Operations** - Use `suspend fun` with coroutines

## Further Reading

- [Clean Architecture by Uncle Bob](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://martinfowler.com/tags/domain%20driven%20design.html)
- [SOLID Principles](https://en.wikipedia.org/wiki/SOLID)
