package com.neptools.app.core.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

data class ImageMeta(
    val uri: Uri,
    val name: String,
    val originalWidth: Int,
    val originalHeight: Int,
    val originalSizeBytes: Long,
    val mimeType: String
)

data class CompressionResult(
    val bitmap: Bitmap,
    val byteArray: ByteArray,
    val width: Int,
    val height: Int,
    val sizeBytes: Long,
    val quality: Int,
    val format: Bitmap.CompressFormat,
    val reductionPercent: Int
)

object ImageCompressorEngine {

    fun extractMetadata(context: Context, uri: Uri): ImageMeta? {
        return try {
            var name = "image_${System.currentTimeMillis()}.jpg"
            var size = 0L
            var mime = "image/jpeg"

            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                val nameIdx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)
                val mimeIdx = cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE)
                if (cursor.moveToFirst()) {
                    if (nameIdx != -1) name = cursor.getString(nameIdx) ?: name
                    if (sizeIdx != -1) size = cursor.getLong(sizeIdx)
                    if (mimeIdx != -1) mime = cursor.getString(mimeIdx) ?: mime
                }
            }

            if (size <= 0L) {
                context.contentResolver.openInputStream(uri)?.use {
                    size = it.available().toLong()
                }
            }

            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            val orientation = getOrientation(context, uri)
            val isRotated = orientation == 90 || orientation == 270
            val w = if (isRotated) options.outHeight else options.outWidth
            val h = if (isRotated) options.outWidth else options.outHeight

            ImageMeta(
                uri = uri,
                name = name,
                originalWidth = max(1, w),
                originalHeight = max(1, h),
                originalSizeBytes = max(1L, size),
                mimeType = mime
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                    ExifInterface.ORIENTATION_ROTATE_90 -> 90
                    ExifInterface.ORIENTATION_ROTATE_180 -> 180
                    ExifInterface.ORIENTATION_ROTATE_270 -> 270
                    else -> 0
                }
            } ?: 0
        } catch (e: Exception) {
            0
        }
    }

    fun loadBitmap(context: Context, uri: Uri, maxDim: Int = 3000): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            }

            var inSampleSize = 1
            val maxOriginal = max(options.outWidth, options.outHeight)
            while ((maxOriginal / (inSampleSize * 2)) >= maxDim) {
                inSampleSize *= 2
            }

            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = inSampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            var bmp = context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOptions)
            } ?: return null

            val orientation = getOrientation(context, uri)
            if (orientation != 0) {
                val matrix = Matrix().apply { postRotate(orientation.toFloat()) }
                val rotated = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                if (rotated != bmp) {
                    bmp.recycle()
                    bmp = rotated
                }
            }
            bmp
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Compresses to target KB with intelligent binary search and adaptive downscaling.
     */
    fun compressToTargetKb(
        src: Bitmap,
        targetKb: Int,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG,
        maxResolutionScale: Float = 1.0f
    ): CompressionResult {
        val targetBytes = (targetKb * 1024L).coerceAtLeast(5 * 1024L)

        // 1. Initial resolution scaling if requested
        var currentBitmap = if (maxResolutionScale < 0.99f) {
            val newW = max(1, (src.width * maxResolutionScale).roundToInt())
            val newH = max(1, (src.height * maxResolutionScale).roundToInt())
            Bitmap.createScaledBitmap(src, newW, newH, true)
        } else {
            src
        }

        var low = 1
        var high = 98
        var bestQuality = 85
        var bestBytes: ByteArray? = null

        // Binary search on compression quality
        for (step in 0..6) {
            val mid = (low + high) / 2
            val baos = ByteArrayOutputStream()
            currentBitmap.compress(format, mid, baos)
            val bytes = baos.toByteArray()

            if (bytes.size <= targetBytes) {
                bestQuality = mid
                bestBytes = bytes
                low = mid + 1 // try higher quality
            } else {
                high = mid - 1
            }
            if (low > high) break
        }

        // If at lowest quality it's still bigger than target, downscale resolution iteratively
        if (bestBytes == null || bestBytes.size > targetBytes) {
            var tempBmp = currentBitmap
            var currentScale = 0.85f

            while (currentScale >= 0.15f) {
                val scaledW = max(1, (src.width * currentScale).roundToInt())
                val scaledH = max(1, (src.height * currentScale).roundToInt())
                val scaledBmp = Bitmap.createScaledBitmap(src, scaledW, scaledH, true)
                val baos = ByteArrayOutputStream()
                scaledBmp.compress(format, 70, baos)
                val bytes = baos.toByteArray()

                if (bytes.size <= targetBytes) {
                    bestBytes = bytes
                    bestQuality = 70
                    tempBmp = scaledBmp
                    break
                }
                currentScale -= 0.15f
            }

            if (bestBytes == null) {
                // Final safety fallback
                val finalW = max(1, (src.width * 0.3f).roundToInt())
                val finalH = max(1, (src.height * 0.3f).roundToInt())
                tempBmp = Bitmap.createScaledBitmap(src, finalW, finalH, true)
                val baos = ByteArrayOutputStream()
                tempBmp.compress(format, 40, baos)
                bestBytes = baos.toByteArray()
                bestQuality = 40
            }
            currentBitmap = tempBmp
        }

        val decodedResult = BitmapFactory.decodeByteArray(bestBytes, 0, bestBytes.size) ?: currentBitmap
        val reduction = if (src.byteCount > 0) {
            val originalEstimate = (src.width * src.height * 3L) // uncompressed RGB
            max(0, (((originalEstimate - bestBytes.size).toDouble() / originalEstimate) * 100).toInt())
        } else 0

        return CompressionResult(
            bitmap = decodedResult,
            byteArray = bestBytes,
            width = currentBitmap.width,
            height = currentBitmap.height,
            sizeBytes = bestBytes.size.toLong(),
            quality = bestQuality,
            format = format,
            reductionPercent = reduction
        )
    }

    /**
     * Resize to explicit dimensions or aspect ratio
     */
    fun resizeToExactDimensions(
        src: Bitmap,
        targetWidth: Int,
        targetHeight: Int,
        quality: Int = 90,
        format: Bitmap.CompressFormat = Bitmap.CompressFormat.JPEG
    ): CompressionResult {
        val scaled = Bitmap.createScaledBitmap(src, targetWidth, targetHeight, true)
        val baos = ByteArrayOutputStream()
        scaled.compress(format, quality, baos)
        val bytes = baos.toByteArray()
        val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: scaled

        return CompressionResult(
            bitmap = decoded,
            byteArray = bytes,
            width = targetWidth,
            height = targetHeight,
            sizeBytes = bytes.size.toLong(),
            quality = quality,
            format = format,
            reductionPercent = 0
        )
    }

    /**
     * Saves compressed byte array to device gallery / MediaStore
     */
    fun saveToGallery(context: Context, bytes: ByteArray, formatName: String = "jpg"): Uri? {
        val filename = "NepTools_Compressed_${System.currentTimeMillis()}.$formatName"
        val mime = if (formatName == "png") "image/png" else if (formatName == "webp") "image/webp" else "image/jpeg"

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val values = ContentValues().apply {
                    put(MediaStore.Images.Media.DISPLAY_NAME, filename)
                    put(MediaStore.Images.Media.MIME_TYPE, mime)
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/NepTools")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }

                val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                if (uri != null) {
                    context.contentResolver.openOutputStream(uri)?.use { it.write(bytes) }
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                    uri
                } else null
            } else {
                val dir = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "NepTools")
                if (!dir.exists()) dir.mkdirs()
                val file = File(dir, filename)
                FileOutputStream(file).use { it.write(bytes) }
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
