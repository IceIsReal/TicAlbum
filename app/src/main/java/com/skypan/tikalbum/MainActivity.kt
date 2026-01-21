package com.skypan.tikalbum

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.skypan.tikalbum.ui.screens.FolderPickerScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // --- 核心修改：双击退出逻辑 ---
                    DoubleBackToExitWrapper {
                        FolderPickerScreen()
                    }
                }
            }
        }
    }
}

/**
 * 一个简单的包装组件，用于实现“再按一次退出”功能
 */
@Composable
fun DoubleBackToExitWrapper(content: @Composable () -> Unit) {
    val context = LocalContext.current
    // 记录上一次按返回键的时间
    var lastBackPressTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastBackPressTime < 2000) {
            // 如果两次点击间隔小于 2 秒，直接退出 Activity
            (context as? android.app.Activity)?.finish()
        } else {
            // 否则，更新时间并弹出提示
            lastBackPressTime = currentTime
            Toast.makeText(context, "再滑动一次退出程序", Toast.LENGTH_SHORT).show()
        }
    }

    // 正常渲染后续内容
    content()
}