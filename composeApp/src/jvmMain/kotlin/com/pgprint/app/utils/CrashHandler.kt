package com.pgprint.app.utils
import com.pgprint.app.BuildConfig.STORED_DIR
import kotlinx.coroutines.CoroutineExceptionHandler
import org.slf4j.LoggerFactory
import java.awt.Desktop
import java.io.File
import javax.swing.JOptionPane
import javax.swing.SwingUtilities
import kotlin.system.exitProcess

object CrashHandler {
    private val logger = LoggerFactory.getLogger("CrashHandler")
    private val logPath = "${System.getProperty("user.home")}/${STORED_DIR}/logs"

    /**
     * 初始化全局异常捕获
     */
    fun init() {
        // 1. 捕获 JVM 线程未处理异常 (如 main 线程或其他普通线程)
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            handleCrash(throwable, "线程 [${thread.name}] 发生致命错误")
        }
    }

    /**
     * 用于协程的异常处理器
     * 使用方式: scope.launch(CrashHandler.coroutineExceptionHandler) { ... }
     */
    val coroutineExceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleCrash(throwable, "异步任务 (Coroutine) 崩溃")
    }

    /**
     * 核心处理逻辑
     */
    fun handleCrash(throwable: Throwable, context: String) {
        // 1. 记录到 Logback 文件
        logger.error("=== 程序崩溃 ===")
        logger.error("场景: $context")
        logger.error("信息: ${throwable.localizedMessage}")
        logger.error("堆栈轨迹:", throwable)

        // 2. 确保在 Swing 事件分发线程中弹出 UI
        SwingUtilities.invokeLater {
            showCrashDialog(throwable, context)
        }
    }

    private fun showCrashDialog(throwable: Throwable, context: String) {
        val message = """
            程序遇到不可恢复的错误已崩溃。
            位置: $context
            异常: ${throwable.javaClass.simpleName}
            详细: ${throwable.localizedMessage ?: "未知错误"}
            
            日志文件已保存至: 
            $logPath
        """.trimIndent()

        val options = arrayOf("关闭程序", "查看日志文件")

        val result = JOptionPane.showOptionDialog(
            null,
            message,
            "系统崩溃",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.ERROR_MESSAGE,
            null,
            options,
            options[0]
        )

        // 如果用户点击“查看日志文件”
        if (result == 1) {
            openLogFolder()
        }

        // 彻底杀死进程（防止后台残留）
        exitProcess(1)
    }

    private fun openLogFolder() {
        try {
            val file = File(logPath)
            if (file.exists()) {
                Desktop.getDesktop().open(file)
            } else {
                // 如果目录还没建立（极少见），尝试打开父目录
                Desktop.getDesktop().open(File(System.getProperty("user.home")))
            }
        } catch (e: Exception) {
            logger.error("无法打开日志文件夹", e)
        }
    }

    fun onHandleOpenLogFolder() {
        openLogFolder()
    }
}