package com.skypan.tikalbum.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.utils.FileHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun FolderPickerScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var mediaList by remember { mutableStateOf<List<MediaModel>>(emptyList()) }
    var isScanning by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri: Uri? ->
        uri?.let { folderUri ->
            isScanning = true
            scope.launch(Dispatchers.IO) {
                val result = FileHelper.scanFolder(context, folderUri)
                withContext(Dispatchers.Main) {
                    mediaList = result
                    isScanning = false
                }
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isScanning) {
            Text("⏳ 正在扫描中...")
        } else if (mediaList.isEmpty()) {
            Text("当前没有导入任何媒体文件")
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { launcher.launch(null) }) {
                Text("选择文件夹")
            }
        } else {
            TikTokScrollPlayer(
                mediaList = mediaList,
                onPickFolder = { launcher.launch(null) }
            )
        }
    }
}