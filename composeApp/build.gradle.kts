import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.lang.System.console


val myAppVersion = "1.0.2"

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    kotlin("plugin.serialization") version "2.3.0"
    id("com.github.gmazzo.buildconfig") version "5.3.5"
    id("app.cash.sqldelight") version "2.2.1"
}

buildConfig {
    // 这行代码会生成 BuildConfig.APP_VERSION 常量
    packageName("com.pgprint.app") // 明确指定包名
    buildConfigField("APP_VERSION", myAppVersion)
    buildConfigField("STORED_DIR", "pgprint")
    buildConfigField("STORED_PREFX", "pgprint_")

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
            implementation("io.ktor:ktor-client-core:3.3.2")
            // 推荐 JVM 引擎
            implementation("io.ktor:ktor-client-okhttp:3.3.2")
            // 序列化（JSON 处理）
            implementation("io.ktor:ktor-client-content-negotiation:3.3.2")
            implementation("io.ktor:ktor-serialization-kotlinx-json:3.3.2")
            // LOG
            implementation("io.ktor:ktor-client-logging:3.3.2")

            // datastore
            implementation("androidx.datastore:datastore:1.2.0")
            implementation("androidx.datastore:datastore-preferences:1.2.0")

            //coil3 网络图片
            implementation("io.coil-kt.coil3:coil-compose:3.3.0")
            //sqldelight
            implementation("app.cash.sqldelight:runtime:2.2.1")

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

            // sqldelight 1. 驱动程序：桌面端（JVM）使用 JDBC 驱动
            implementation("app.cash.sqldelight:sqlite-driver:2.2.1")
            implementation("app.cash.sqldelight:coroutines-extensions-jvm:2.2.1")
            // ./gradlew :composeApp:generateCommonMainAppDatabaseInterface 手动同步 sql表以及结构的创建

            //coil3 网络图片
            implementation("io.coil-kt.coil3:coil-network-okhttp:3.3.0") // Only available on Android/JVM.
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
                menuGroup = "pgprinter"
                shortcut = true
                menu = true
                dirChooser = true
                upgradeUuid = "550e8400-e29b-41d4-a716-446655440000"
                includeAllModules = true
                console = true
            }
            macOS {
                bundleID = "com.pgprint.app"
                packageName = "pgprinter"
                console()
            }
        }
    }
}

sqldelight {
    databases {
        create("AppDatabase") {
            packageName.set("com.pgprint.app.db") // 生成代码的包名
            // srcDirs.setFrom("src/jvmMain/sqldelight")
        }
    }
}

