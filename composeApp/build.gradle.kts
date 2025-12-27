import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import java.lang.System.console


plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}

kotlin {
    jvm()
    sourceSets {
        commonMain.dependencies {

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

            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}


compose.desktop {
    application {
        mainClass = "com.pgprint.app.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe, TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "PGPrint"
            packageVersion = "1.0.0"
            copyright = "2025 BUTCOMPANY"
            description = "2025 BUTCOMPANY"
            windows {
                shortcut = true
                menu = true
                dirChooser = true
                upgradeUuid = "550e8400-e29b-41d4-a716-446655440000"
                includeAllModules = true
                console = true
            }
            macOS {
                bundleID = "com.pgprint.app"
                packageName = "PG"
                console()
            }
        }
    }
}
