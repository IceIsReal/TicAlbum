package com.skypan.tikalbum.ui.components

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource // 关键导入
import androidx.compose.ui.unit.dp
import com.skypan.tikalbum.R // 导入你的 R 文件

@SuppressLint("SourceLockedOrientationActivity")
@Composable
fun TopControlBar(
    onFolderClick: () -> Unit,
    onRotateClick: () -> Unit,
    onModeClick: () -> Unit,
    currentModeName: String,
    onSpeedClick: () -> Unit,
    currentSpeed: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 16.dp, end = 16.dp)
            .height(56.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // --- 文件夹选择按钮：更换为自定义图标 ---
        IconButton(
            onClick = onFolderClick,
            modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
        ) {
            Icon(
                // 假设你的图片叫 ic_custom_folder.xml
                painter = painterResource(id = R.drawable.folder),
                contentDescription = "Folder",
                tint = Color.Unspecified // 如果你的图标自带颜色，设为 Unspecified
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextButton(
                onClick = onSpeedClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Text(text = "${currentSpeed}x", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }

            TextButton(
                onClick = onModeClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
            ) {
                Text(text = currentModeName, color = Color.White, style = MaterialTheme.typography.labelSmall)
            }

            // --- 旋转按钮：更换为自定义图标 ---
            IconButton(
                onClick = onRotateClick,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                   .size(40.dp)
            ) {
                Icon(
                    // 假设你的图片叫 ic_custom_rotate.xml
                    painter = painterResource(id = R.drawable.exchange),
                    contentDescription = "Rotate",
                    tint = Color.White
                )
            }
        }
    }
}