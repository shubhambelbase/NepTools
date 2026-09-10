package com.neptools.app.core.converter

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min

object ImageToPdfEngine {

    suspend fun createPdf(
        context: Context,
        imageUris: List<Uri>,
        pageSize: ConverterPageSize,
        quality: Int,
        onProgress: (Int, Int) -> Unit,
        onBitmapDecoded: (Int, Bitmap) -> Unit = { _, _ -> }
    ): File? {
        if (imageUris.isEmpty()) return null
        val pdf = PdfDocument()
        var pageCount = 0
        try {
            for ((idx, uri) in imageUris.withIndex()) {
                onProgress(idx + 1, imageUris.size)
                val bmp = decodeWithExif(context, uri) ?: continue
                onBitmapDecoded(idx, bmp)

                val (pw, ph) = resolvePageSize(bmp, pageSize)
                val pageInfo = PdfDocument.PageInfo.Builder(pw.toInt(), ph.toInt(), idx + 1).create()
                val page = pdf.startPage(pageInfo)
                drawBitmapCentered(page.canvas, bmp, pw, ph)
                pdf.finishPage(page)
                pageCount++
            }

            if (pageCount == 0) return null

            val outFile = File(context.cacheDir, "converter/images_to_pdf_${System.currentTimeMillis()}.pdf")
            outFile.parentFile?.mkdirs()
            FileOutputStream(outFile).use { fos ->
                pdf.writeTo(fos)
                fos.flush()
            }
            return outFile
        } finally {
            pdf.close()
        }
    }

    fun resolvePageSize(bmp: Bitmap, size: ConverterPageSize): Pair<Float, Float> {
        if (size == ConverterPageSize.ORIGINAL) {
            val scale = 72f / 150f
            var w = bmp.width * scale
            var h = bmp.height * scale
            val maxSide = 1000f
            val longest = maxOf(w, h)
            if (longest > maxSide) {
                val s = maxSide / longest
                w *= s; h *= s
            }
            return Pair(w.coerceAtLeast(72f), h.coerceAtLeast(72f))
        }
        var pw = size.widthPt
        var ph = size.heightPt
        val isLandscapeImage = bmp.width > bmp.height
        val isPagePortrait = pw < ph
        if (isLandscapeImage && isPagePortrait) {
            val tmp = pw; pw = ph; ph = tmp
        }
        return Pair(pw, ph)
    }

    private fun drawBitmapCentered(canvas: Canvas, bmp: Bitmap, pageW: Float, pageH: Float) {
        canvas.drawColor(android.graphics.Color.WHITE)
        val margin = 18f
        val availW = pageW - margin * 2
        val availH = pageH - margin * 2
        val scale = min(availW / bmp.width, availH / bmp.height).coerceAtMost(1f)
        val drawW = bmp.width * scale
        val drawH = bmp.height * scale
        val left = (pageW - drawW) / 2f
        val top = (pageH - drawH) / 2f
        val paint = Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)
        canvas.drawBitmap(bmp, null, android.graphics.RectF(left, top, left + drawW, top + drawH), paint)
    }

    fun decodeWithExif(context: Context, uri: Uri): Bitmap? {
        return try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return null
            val bmp = pfd.use { fd ->
                BitmapFactory.Options().let { opts ->
                    opts.inPreferredConfig = Bitmap.Config.ARGB_8888
                    BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, opts)
                }
            } ?: return null

            val orientation = try {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
                } ?: ExifInterface.ORIENTATION_NORMAL
            } catch (_: Throwable) { ExifInterface.ORIENTATION_NORMAL }

            when (orientation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> rotateBitmap(bmp, 90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> rotateBitmap(bmp, 180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> rotateBitmap(bmp, 270f)
                else -> bmp
            }
        } catch (_: Throwable) {
            null
        } catch (_: OutOfMemoryError) {
            null
        }
    }

    private fun rotateBitmap(src: Bitmap, degrees: Float): Bitmap {
        val m = android.graphics.Matrix()
        m.postRotate(degrees)
        return try {
            val out = Bitmap.createBitmap(src, 0, 0, src.width, src.height, m, true)
            if (out !== src) src.recycle()
            out
        } catch (_: Throwable) { src }
    }
}
