package com.skypan.tikalbum

import android.net.Uri

// 这里的 mediaType 用来区分是图片还是视频
enum class MediaType { IMAGE, VIDEO }

data class MediaModel(
    val uri: Uri,          // 文件的真实路径
    val name: String,     // 文件名
    val date: String,     // 拍摄或修改时间
    val type: MediaType   // 类型
)