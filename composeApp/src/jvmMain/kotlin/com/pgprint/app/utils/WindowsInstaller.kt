package com.pgprint.app.utils

import java.io.File

object WindowsInstaller {

    fun install(msiFile: File) {
        val ps = File(msiFile.parentFile, "update.ps1")

        ps.writeText(
            """
            Start-Sleep -Seconds 2
            Start-Process msiexec `
              -ArgumentList "/i `"${
                msiFile.absolutePath
            }`" /quiet /norestart" `
              -Wait
            """.trimIndent()
        )

        ProcessBuilder(
            "powershell",
            "-ExecutionPolicy", "Bypass",
            "-File", ps.absolutePath
        ).start()
    }
}
