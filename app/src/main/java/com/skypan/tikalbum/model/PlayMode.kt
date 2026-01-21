package com.skypan.tikalbum.model

// 修复点：改名为列表循环，逻辑更清晰
enum class PlayMode(val label: String) {
    SINGLE_LOOP("单独循环"),
    LIST_LOOP("列表循环")
}