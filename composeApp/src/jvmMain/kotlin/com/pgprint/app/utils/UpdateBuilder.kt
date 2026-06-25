package com.pgprint.app.utils

import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import kotlin.system.exitProcess
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object UpdateBuilder {
     fun checkVelopackUpdate() {
         GlobalScope.launch {
             try {
                 // 1. 定位本地的 Update 可执行程序
                 // Velopack 运行状态下，Update.exe 通常在当前运行目录的上一级 (Current 的父目录)
                 val currentDir = File(System.getProperty("user.dir"))
                 val os = System.getProperty("os.name").lowercase()

                 val updateExe = if (os.contains("win")) {
                     File(currentDir.parentFile, "Update.exe")
                 } else {
                     File(currentDir.parentFile, "Update") // macOS / Linux
                 }
                 if (!updateExe.exists()) {
                     println("未找到 Velopack 环境（可能处于本地开发调测阶段）")
                     return@launch
                 }

                 // 2. 构造本地调用命令：检查并下载更新
                 // 这里的远程 URL 换成你的服务器地址或维持先前 GitHub Actions 自动发布的仓库地址
                 val updateUrl = "http://sm.butsd.com/pgprinter-updates"

                 val process = ProcessBuilder(
                     updateExe.absolutePath,
                     "checkForUpdate",
                     "--url", updateUrl
                 ).start()

                 val reader = BufferedReader(InputStreamReader(process.inputStream))
                 val output = reader.readText()
                 process.waitFor()

                 // 3. 判断是否有新版本并下载完毕
                 if (process.exitValue() == 0 && output.contains("Update available")) {
                     println("检测到新版本并已在后台准备就绪！")

                     // 提示用户：可以在这里挂载 UI 弹窗询问用户是否“立即重启升级”
                     // 如果用户同意，调用下方的应用更新重启命令：
                     applyUpdateAndRestart(updateExe)
                 }
             } catch (e: Exception) {
                 println(e)
             }
         }
    }

    // 当用户同意重启升级时调用此函数
    fun applyUpdateAndRestart(updateExe: File) {
        try {
            // 让 Velopack 启动新版本并关闭当前进程
            ProcessBuilder(updateExe.absolutePath, "swap").start()
            exitProcess(0)
        } catch (e: Exception) {
            println(e.message)
        }
    }
}