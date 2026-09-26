package com.neptools.app.core.util

import android.content.ClipData
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.toClipEntry

suspend fun Clipboard.setPlainText(text: String, label: String = "NepTools") {
    val clip = ClipData.newPlainText(label, text)
    setClipEntry(clip.toClipEntry())
}

suspend fun Clipboard.getPlainText(): String? {
    val entry = getClipEntry() ?: return null
    val clipData = entry.clipData
    return if (clipData.itemCount > 0) {
        clipData.getItemAt(0).text?.toString()
    } else {
        null
    }
}
