import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.lang.System.console


val myAppVersion = "1.0.1"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.3.0"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
}

buildConfig {
    // 这行代码会生成 BuildConfig.APP_VERSION 常量
    packageName("com.pgprint.app") // 明确指定包名
    buildConfigField("APP_VERSION", myAppVersion)
    buildConfigField("STORED_DIR", "pgprint")

}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {

            implementation("com.arkivanov.decompose:decompose:3.2.2")
            // 2. Decompose 对 Compose 的支持库 (用于 Children() 等)
            implementation("com.arkivanov.decompose:extensions-compose:3.2.2")

            // 3. Kotlinx Serialization JSON (用于保存导航状态)
            implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0-RC")

            // Ktor 核心
            implementation("io.ktor:ktor-client-core:3.0.0")
            // 推荐 JVM 引擎
            implementation("io.ktor:ktor-client-okhttp:3.0.0")
            // 序列化（JSON 处理）
            implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
            // LOG
            implementation("io.ktor:ktor-client-logging:3.0.0")
            //ehcache 缓存
            implementation("org.ehcache:ehcache:3.10.8")

            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            // 查询串口/USB
            implementation("com.fazecast:jSerialComm:2.10.4")
            implementation("org.usb4java:usb4java-javax:1.3.0")

            // escpos
            implementation("com.github.anastaciocintra:escpos-coffee:4.1.0")
            // logback 日志
            implementation("ch.qos.logback:logback-classic:1.5.6")
            implementation("org.slf4j:slf4j-api:2.0.13")

            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.pgprint.app.MainKt"
        jvmArgs(
            "-Dfile.encoding=UTF-8",
            "-Dsun.jnu.encoding=UTF-8"
        )
        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "pgprinter"
            packageVersion = myAppVersion
            copyright = "2025 BUTCOMPANY"
            description = "2025 BUTCOMPANY"
            windows {
                menuGroup = "PG外卖打印"
                shortcut = true
                menu = true
                dirChooser = true
                upgradeUuid = "550e8400-e29b-41d4-a716-446655440000"
                includeAllModules = true
                // console = true
            }
            macOS {
                bundleID = "com.pgprint.app"
                packageName = "pgprinter"
                console()
            }
        }
    }
}
