package com.pgprint.app.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.pgprint.app.model.UpdateState

@Composable
fun UpdateDialog(
    state: UpdateState,
    startDownload: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.width(420.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = 8.dp
    ) {
        Column(Modifier.padding(24.dp)) {
            Button(
                onClick = startDownload
            ) {
                Text(
                    text = "开始更新应用",
                )
            }
            Text(
                text = "更新应用",
            )
            Spacer(Modifier.height(16.dp))
            when (state) {
                is UpdateState.Checking ->
                    Text("检查更新中...")

                is UpdateState.Downloading -> {
                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "${state.downloaded / 1024} KB / ${state.total / 1024} KB"
                    )
                }

                UpdateState.Installing ->
                    Text("正在安装新版本，请稍候...")

                is UpdateState.Error ->
                    Text(
                        "更新失败：${state.message}",
                        color = Color.Red
                    )
                else -> {}
            }
        }
    }
}
