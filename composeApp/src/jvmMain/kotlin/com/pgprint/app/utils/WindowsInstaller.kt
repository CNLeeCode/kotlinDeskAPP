package com.pgprint.app.utils

import java.io.File

object WindowsInstaller {

    fun install(msiFile: File) {
        val ps = File(msiFile.parentFile, "update.ps1")

        // 优化脚本逻辑
        ps.writeText(
            """
        # 等待旧程序彻底退出
        Start-Sleep -Seconds 3
        
        # 启动安装程序并请求管理员权限 (RunAs)
        # /i 安装, /qn 真正静默, /norestart 不重启电脑
        # 使用 -Wait 确保安装完成后才结束脚本
        Start-Process msiexec -ArgumentList "/i `"${msiFile.absolutePath}`" /qn /norestart" -Verb RunAs -Wait
        
        # (可选) 如果需要安装完自动重启应用，可以在这里添加 start-process 你的exe路径
        """.trimIndent()
        )

        // 使用 ProcessBuilder 启动，但不等待它结束
        val pb = ProcessBuilder(
            "powershell",
            "-NoProfile",
            "-ExecutionPolicy", "Bypass",
            "-WindowStyle", "Hidden", // 隐藏控制台窗口
            "-File", ps.absolutePath
        )
        // 关键点：将 stdout/stderr 重定向到文件，方便你排查为什么失败
        pb.redirectError(File(msiFile.parentFile, "update_error.log"))
        pb.start()
    }
}
