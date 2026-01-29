package com.pgprint.app.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URL
import javax.sound.sampled.AudioSystem
import javax.sound.sampled.Clip


object DesktopAudioPlayer {
    private var clip: Clip? = null

    // 记录上一次播放成功的时间戳（毫秒）
    private var lastPlayTime: Long = 0

    // 定义冷却时间（30秒 = 30000毫秒）
    private const val COOL_DOWN_MILLIS: Long = 6000

    suspend fun play(fileName: String, forced: Boolean = false) {

        val currentTime = System.currentTimeMillis()

        // 1. 检查是否在冷却时间内
        if (!forced && currentTime - lastPlayTime < COOL_DOWN_MILLIS) {
            val remainingSeconds = (COOL_DOWN_MILLIS - (currentTime - lastPlayTime)) / 1000
            println("播放过于频繁，处于冷却中。剩余等待时间: ${remainingSeconds}s")
            return
        }
        try {
            val file = Utils.getAudioFile(fileName)
            if (!file.exists()) {
                println("音频文件不存在: ${file.absolutePath}")
                return
            }

            // 1. 获取音频输入流
            val audioStream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(file)
            }
            // 2. 获取 Clip 实例
            val currentClip = AudioSystem.getClip()

            // 3. 停止并关闭之前的播放
            stop()

            // 4. 打开流并开始播放
            currentClip.open(audioStream)
            currentClip.start()

            clip = currentClip
            if (!forced) {
                lastPlayTime = currentTime
            }

            // 播放完成后自动释放资源（可选）
            currentClip.addLineListener { event ->
                if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                    currentClip.close()
                }
            }

        } catch (e: Exception) {
            println("播放失败: ${e.message}")
            e.printStackTrace()
        }
    }

    suspend fun play2(url: URL, forced: Boolean = false) {

        val currentTime = System.currentTimeMillis()

        if (!forced && currentTime - lastPlayTime < COOL_DOWN_MILLIS) {
            val remainingSeconds =
                (COOL_DOWN_MILLIS - (currentTime - lastPlayTime)) / 1000
            println("播放过于频繁，冷却中，剩余: ${remainingSeconds}s")
            return
        }

        try {
            // 1️⃣ 只把「流读取」放到 IO
            val audioStream = withContext(Dispatchers.IO) {
                AudioSystem.getAudioInputStream(url)
            }

            // 2️⃣ 所有 Clip 操作在当前线程
            stop()
            val currentClip = AudioSystem.getClip()
            currentClip.open(audioStream)
            currentClip.start()

            clip = currentClip
            if (!forced) lastPlayTime = currentTime

            currentClip.addLineListener { event ->
                if (event.type == javax.sound.sampled.LineEvent.Type.STOP) {
                    currentClip.close()
                }
            }

        } catch (e: Exception) {
            println("播放失败: ${e.message}")
            e.printStackTrace()
        }
    }

    fun stop() {
        clip?.let {
            if (it.isRunning) it.stop()
            it.close()
        }
        clip = null
    }
}