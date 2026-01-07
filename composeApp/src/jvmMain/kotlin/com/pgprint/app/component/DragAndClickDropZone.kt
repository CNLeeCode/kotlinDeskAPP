package com.pgprint.app.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.onDrag
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragData
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.pgprint.app.utils.AppColors
import com.pgprint.app.utils.PersistentCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.painterResource
import pgprint.composeapp.generated.resources.Res
import pgprint.composeapp.generated.resources.choosefile
import pgprint.composeapp.generated.resources.placeholder
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

@OptIn( ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun DragAndClickDropZone(
    modifier: Modifier = Modifier,
    targetDirPath: String = PersistentCache.cacheDir.absolutePath
) {
    val scope = rememberCoroutineScope()
    var imageCacheKey by remember {
        mutableStateOf("kf-photo_0")
    }

    Column(
        modifier = modifier.fillMaxSize().padding(30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier.size(200.dp).border(1.dp, Color.Gray),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = ImageRequest.Builder(coil3.compose.LocalPlatformContext.current) // 注意这里！使用 Coil 提供的平台上下文
                    .data(File(targetDirPath,"kf-photo.jpg"))
                    .memoryCacheKey(imageCacheKey)
                    .crossfade(true)
                    .build(),
                contentDescription = "本地图片",
                modifier = Modifier.size(200.dp),
                error = painterResource(Res.drawable.placeholder),
                placeholder = painterResource(Res.drawable.placeholder),
            )
        }
        Spacer(Modifier.height(30.dp))
        Button(
            modifier = Modifier.width(200.dp).pointerHoverIcon(PointerIcon.Crosshair),
            onClick = {
                val selectedFiles = openFileDialog()
                if (selectedFiles.isNotEmpty()) {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            saveFiles(selectedFiles, targetDirPath, "kf-photo.jpg")
                        }
                        delay(500)
                        imageCacheKey = "kf-photo_${Math.random()}"
                    }
                }
            },
            shape = RoundedCornerShape(2.dp),
            colors =  ButtonDefaults.buttonColors(
                containerColor = Color.White
            ),
            border = BorderStroke(1.dp, AppColors.PrimaryColor)
        ) {
            Row (
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Icon(
                    painter = painterResource(Res.drawable.choosefile),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = AppColors.PrimaryColor
                )
                Spacer(Modifier.height(8.dp))
                Text("点击选择文件", color = AppColors.PrimaryColor)
            }
        }
    }
}

/**
 * 使用 AWT FileDialog 打开系统原生选择框
 */
fun openFileDialog(): List<File> {
    val dialog = FileDialog(null as Frame?, "选择文件", FileDialog.LOAD).apply {
        isMultipleMode = true // 允许选择多个文件
        isVisible = true
    }

    return dialog.files.toList()
}

/**
 * 统一的保存逻辑
 */
fun saveFiles(files: List<File>, targetDirPath: String, fileName: String = "") {
    val targetDir = File(targetDirPath)
    if (!targetDir.exists()) targetDir.mkdirs()
    files.forEach { file ->
        val dest = File(targetDir, fileName.ifEmpty { file.name })
        Files.copy(file.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING)
        println("已保存到: ${dest.absolutePath}")
    }
}