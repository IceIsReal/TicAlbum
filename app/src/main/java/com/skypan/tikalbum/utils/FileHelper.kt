package com.skypan.tikalbum.utils

import android.content.Context
import android.net.Uri
import androidx.documentfile.provider.DocumentFile
import com.skypan.tikalbum.model.MediaModel
import com.skypan.tikalbum.model.MediaType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FileHelper {
    private val imageExtensions = listOf("jpg", "jpeg", "png", "webp", "gif")
    private val videoExtensions = listOf("mp4", "mkv", "webm", "avi")

    fun scanFolder(context: Context, treeUri: Uri): List<MediaModel> {
        val mediaList = mutableListOf<MediaModel>()
        val rootFolder = DocumentFile.fromTreeUri(context, treeUri)

        // 定义日期格式化工具
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        if (rootFolder != null && rootFolder.isDirectory) {
            rootFolder.listFiles().forEach { file ->
                val name = file.name ?: "Unknown"
                val extension = name.substringAfterLast(".", "").lowercase()

                val type = when {
                    videoExtensions.contains(extension) -> MediaType.VIDEO
                    imageExtensions.contains(extension) -> MediaType.IMAGE
                    else -> null
                }

                if (type != null) {
                    // --- 核心修改：获取真实的最后修改时间 ---
                    val formattedDate = sdf.format(Date(file.lastModified()))

                    mediaList.add(
                        MediaModel(
                            uri = file.uri,
                            name = name,
                            date = formattedDate, // 存入真实时间
                            type = type
                        )
                    )
                }
            }
        }
        return mediaList
    }
}