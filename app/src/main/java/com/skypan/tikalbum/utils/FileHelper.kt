package com.skypan.tikalbum.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.MediaType

object FileHelper {
    // 支持的后缀名清单
    private val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
    private val videoExtensions = listOf("mp4", "mkv", "webm", "avi")

    fun scanFolder(context: Context, treeUri: Uri): List<MediaModel> {
        val mediaList = mutableListOf<MediaModel>()

        // 1. 根据 Uri 拿到对应的文件夹对象
        val rootFolder = DocumentFile.fromTreeUri(context, treeUri)

        if (rootFolder != null && rootFolder.isDirectory) {
            // 2. 遍历文件夹下的所有文件
            rootFolder.listFiles().forEach { file ->
                val name = file.name ?: "Unknown"
                val extension = name.substringAfterLast(".", "").lowercase()

                // 3. 判断是图片还是视频
                val type = when {
                    videoExtensions.contains(extension) -> MediaType.VIDEO
                    imageExtensions.contains(extension) -> MediaType.IMAGE
                    else -> null
                }

                // 4. 如果是我们要的内容，就存入列表
                if (type != null) {
                    mediaList.add(
                        MediaModel(
                            uri = file.uri,
                            name = name,
                            date = "未知时间", // 暂时填占位符，后面再优化
                            type = type
                        )
                    )
                }
            }
        }
        return mediaList
    }
}