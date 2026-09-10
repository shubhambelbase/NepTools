package com.neptools.app.core.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

object PdfToImageEngine {

    fun getPageCount(context: Context, pdfUri: Uri): Int {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        return try {
            pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return 0
            val cacheFile = File(context.cacheDir, "converter/pdf_${System.currentTimeMillis()}.pdf")
            cacheFile.parentFile?.mkdirs()
            FileOutputStream(cacheFile).use { out ->
                FileInputStream(pfd.fileDescriptor).use { input ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) out.write(buf, 0, n)
                }
            }
            val pfd2 = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd2)
            val count = renderer.pageCount
            renderer.close()
            pfd2.close()
            cacheFile.delete()
            count
        } catch (_: Throwable) {
            0
        } finally {
            try { renderer?.close() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
        }
    }

    fun renderPage(
        context: Context,
        pdfUri: Uri,
        pageIndex: Int,
        dpi: Int,
        format: PdfImageFormat
    ): Bitmap? {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var cacheFile: File? = null
        return try {
            pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return null
            cacheFile = File(context.cacheDir, "converter/pdf_render_${System.currentTimeMillis()}.pdf")
            cacheFile.parentFile?.mkdirs()
            FileOutputStream(cacheFile).use { out ->
                FileInputStream(pfd.fileDescriptor).use { input ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) out.write(buf, 0, n)
                }
            }
            pfd.close(); pfd = null

            val pfd2 = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd2)
            if (pageIndex < 0 || pageIndex >= renderer.pageCount) return null

            val page = renderer.openPage(pageIndex)
            val pw = page.width
            val ph = page.height
            val scale = dpi / 72f
            val bmpW = (pw * scale).toInt().coerceIn(1, 5000)
            val bmpH = (ph * scale).toInt().coerceIn(1, 5000)

            val bmp = Bitmap.createBitmap(bmpW, bmpH, Bitmap.Config.ARGB_8888)
            bmp.eraseColor(android.graphics.Color.WHITE)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            renderer.close()
            pfd2.close()
            cacheFile.delete()
            bmp
        } catch (_: Throwable) {
            null
        } finally {
            try { renderer?.close() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
            try { cacheFile?.delete() } catch (_: Throwable) {}
        }
    }

    fun renderThumbnails(
        context: Context,
        pdfUri: Uri,
        thumbDpi: Int = 48,
        onEach: (Int, Bitmap) -> Unit
    ): Int {
        var pfd: ParcelFileDescriptor? = null
        var renderer: PdfRenderer? = null
        var cacheFile: File? = null
        return try {
            pfd = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return 0
            cacheFile = File(context.cacheDir, "converter/pdf_thumb_${System.currentTimeMillis()}.pdf")
            cacheFile.parentFile?.mkdirs()
            FileOutputStream(cacheFile).use { out ->
                FileInputStream(pfd.fileDescriptor).use { input ->
                    val buf = ByteArray(8192)
                    var n: Int
                    while (input.read(buf).also { n = it } != -1) out.write(buf, 0, n)
                }
            }
            pfd.close(); pfd = null
            val pfd2 = ParcelFileDescriptor.open(cacheFile, ParcelFileDescriptor.MODE_READ_ONLY)
            renderer = PdfRenderer(pfd2)
            val count = renderer.pageCount
            for (i in 0 until count) {
                val page = renderer.openPage(i)
                val scale = thumbDpi / 72f
                val bmp = Bitmap.createBitmap(
                    (page.width * scale).toInt().coerceIn(1, 800),
                    (page.height * scale).toInt().coerceIn(1, 800),
                    Bitmap.Config.ARGB_8888
                )
                bmp.eraseColor(android.graphics.Color.WHITE)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                onEach(i, bmp)
            }
            renderer.close()
            pfd2.close()
            cacheFile.delete()
            count
        } catch (_: Throwable) {
            0
        } finally {
            try { renderer?.close() } catch (_: Throwable) {}
            try { pfd?.close() } catch (_: Throwable) {}
            try { cacheFile?.delete() } catch (_: Throwable) {}
        }
    }
}
