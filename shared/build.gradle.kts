import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties
import com.codingfeline.buildkonfig.compiler.FieldSpec.Type
import org.gradle.api.tasks.Exec
import org.gradle.internal.os.OperatingSystem

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.kotlinSerialization)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.buildkonfig)
}

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }
    
    val xcfName = "Shared"

    iosX64 {
      binaries.framework {
        baseName = xcfName
      }
    }

    iosArm64 {
      binaries.framework {
        baseName = xcfName
      }
    }

    iosSimulatorArm64 {
      binaries.framework {
        baseName = xcfName
      }
    }
    
    sourceSets {
        commonMain.dependencies {
            // Compose Multiplatform
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // Serialization
            implementation(libs.kotlinx.serialization.json)
            
            // Ktor Client
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            implementation(libs.ktor.client.websockets)
            
            // Koin
            implementation(libs.koin.core)
            
            // Voyager (Navigation)
            implementation(libs.voyager.navigator)
            implementation(libs.voyager.screenmodel)
            implementation(libs.voyager.koin)

            // Clock
            implementation(libs.kotlinx.datetime)
        }
        
        androidMain.dependencies {
            implementation(libs.ktor.client.okhttp)
            implementation(libs.kotlinx.coroutines.android)
            implementation(libs.koin.android)
            
            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
        
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
            implementation(libs.ui.uikit)
        }
    }
}

android {
    namespace = "com.mcpchat.shared"
    compileSdk = (property("android.compileSdk") as String).toInt()
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    defaultConfig {
        minSdk = (property("android.minSdk") as String).toInt()
    }
}

val lp = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

buildkonfig {
    packageName = "mcpchatapp.shared" // nơi sinh class BuildKonfig

    defaultConfigs {
        // Ưu tiên lấy từ local.properties, nếu thiếu dùng fallback
        buildConfigField(
            Type.STRING,
            "MCP_SERVER_URL",
            lp.getProperty("MCP_SERVER_URL","")
        )
        buildConfigField(Type.STRING, "CLAUDE_API_KEY", lp.getProperty("CLAUDE_API_KEY", ""))
    }
}

tasks.register<Exec>("stripXattrsKmpOutputs") {
    onlyIf { OperatingSystem.current().isMacOsX }
    commandLine("bash", "-lc",
        "xattr -rc \"${layout.buildDirectory.get().asFile.absolutePath}\" || true"
    )
    isIgnoreExitValue = true
}

tasks.matching { it.name == "embedAndSignAppleFrameworkForXcode" }.configureEach {
    dependsOn("stripXattrsKmpOutputs")
}
