package com.neptools.app.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.neptools.app.ui.screens.DishItem
import com.neptools.app.ui.screens.GroupMember
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReceiptGraphicGenerator {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.US)

    fun createQuickSplitReceipt(
        context: Context,
        rawBill: Double,
        tipPercent: Float,
        tipAmount: Double,
        serviceChargeAmount: Double,
        vatAmount: Double,
        totalBill: Double,
        numPeople: Int,
        perPersonAmount: Double,
        isEn: Boolean
    ): File {
        val width = 1080
        val height = 1420
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background canvas fill
        canvas.drawColor(Color.parseColor("#0F172A")) // Modern dark slate outer canvas

        val cardMargin = 40f
        val cardRect = RectF(cardMargin, cardMargin, width - cardMargin, height - cardMargin)

        // Receipt Card (Clean Warm Paper White)
        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFFFFF")
            style = Paint.Style.FILL
            setShadowLayer(30f, 0f, 15f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, cardPaint)

        // Header Banner (Deep Navy Gradient Block)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val headerPath = Path().apply {
            addRoundRect(
                cardRect.left, cardRect.top, cardRect.right, cardRect.top + 200f,
                floatArrayOf(36f, 36f, 36f, 36f, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(headerPath, headerPaint)

        // Header Text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 44f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "DINING BILL & TIP SPLIT" else "रेस्टुरेन्ट बिल तथा टिप रसिद",
            width / 2f,
            cardRect.top + 90f,
            titlePaint
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd • hh:mm a", Locale.US).format(Date())
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NepTools • $dateStr", width / 2f, cardRect.top + 145f, datePaint)

        // Perforated Dashed Line
        var currentY = cardRect.top + 240f
        drawDashedLine(canvas, cardRect.left + 30f, cardRect.right - 30f, currentY)

        currentY += 50f

        // Hero Per-Person Highlight Card
        val heroRect = RectF(cardRect.left + 40f, currentY, cardRect.right - 40f, currentY + 220f)
        val heroPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        val heroStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#059669")
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        canvas.drawRoundRect(heroRect, 24f, 24f, heroPaint)
        canvas.drawRoundRect(heroRect, 24f, 24f, heroStrokePaint)

        val heroLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#059669")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "${if (isEn) "EACH PERSON PAYS" else "प्रत्येकले तिर्नुपर्ने रकम"} ($numPeople ${if (isEn) "People" else "जना"})",
            width / 2f,
            heroRect.top + 65f,
            heroLabelPaint
        )

        val heroAmountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 68f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Rs. ${numberFormat.format(perPersonAmount.toLong())}", width / 2f, heroRect.top + 155f, heroAmountPaint)

        currentY += 270f

        // Itemized Breakdown Table
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.LEFT
        }
        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }

        fun drawRow(label: String, value: String) {
            canvas.drawText(label, cardRect.left + 50f, currentY, labelPaint)
            canvas.drawText(value, cardRect.right - 50f, currentY, valPaint)
            currentY += 60f
        }

        drawRow(if (isEn) "Base Bill Amount" else "मूल बिल रकम", "Rs. ${numberFormat.format(rawBill.toLong())}")
        drawRow("${if (isEn) "Tip" else "टिप"} (${tipPercent.toInt()}%)", "Rs. ${numberFormat.format(tipAmount.toLong())}")

        if (serviceChargeAmount > 0) {
            drawRow(if (isEn) "Service Charge (10%)" else "सेवा शुल्क (१०%)", "Rs. ${numberFormat.format(serviceChargeAmount.toLong())}")
        }
        if (vatAmount > 0) {
            drawRow(if (isEn) "Govt VAT (13%)" else "सरकारी भ्याट (१३%)", "Rs. ${numberFormat.format(vatAmount.toLong())}")
        }

        drawRow(if (isEn) "Number of People" else "मानिसहरूको सङ्ख्या", "$numPeople ${if (isEn) "Persons" else "जना"}")

        currentY += 10f
        drawDashedLine(canvas, cardRect.left + 30f, cardRect.right - 30f, currentY)
        currentY += 60f

        // Total Bill Grand Row
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val totalValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E11D48")
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(if (isEn) "GRAND TOTAL" else "जम्मा कुल बिल", cardRect.left + 50f, currentY, totalLabelPaint)
        canvas.drawText("Rs. ${numberFormat.format(totalBill.toLong())}", cardRect.right - 50f, currentY, totalValPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✓ Verified Bill Split • Nepal Patro NepTools", width / 2f, cardRect.bottom - 40f, footerPaint)

        return saveBitmapToCache(context, bitmap, "quick_split_receipt.png")
    }

    fun createItemizedReceipt(
        context: Context,
        members: List<GroupMember>,
        dishes: List<DishItem>,
        memberSubtotals: Map<String, Double>,
        totalMultiplier: Double,
        subtotalAll: Double,
        isEn: Boolean
    ): File {
        val width = 1080
        val baseHeight = 900
        val itemHeight = (members.size * 180) + (dishes.size * 40)
        val height = (baseHeight + itemHeight).coerceAtMost(2800)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        canvas.drawColor(Color.parseColor("#0F172A"))

        val cardMargin = 40f
        val cardRect = RectF(cardMargin, cardMargin, width - cardMargin, height - cardMargin)

        val cardPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(30f, 0f, 15f, Color.argb(100, 0, 0, 0))
        }
        canvas.drawRoundRect(cardRect, 36f, 36f, cardPaint)

        // Header Banner
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val headerPath = Path().apply {
            addRoundRect(
                cardRect.left, cardRect.top, cardRect.right, cardRect.top + 190f,
                floatArrayOf(36f, 36f, 36f, 36f, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(headerPath, headerPaint)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "ITEMIZED DISH SPLIT SETTLEMENT" else "व्यक्तिगत परिकार हिसाब रसिद",
            width / 2f,
            cardRect.top + 85f,
            titlePaint
        )

        val dateStr = SimpleDateFormat("yyyy-MM-dd • hh:mm a", Locale.US).format(Date())
        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NepTools Group Split • $dateStr", width / 2f, cardRect.top + 140f, datePaint)

        var currentY = cardRect.top + 230f
        drawDashedLine(canvas, cardRect.left + 30f, cardRect.right - 30f, currentY)
        currentY += 50f

        // Member Cards
        members.forEach { member ->
            val base = memberSubtotals[member.id] ?: 0.0
            val memberTotal = base * totalMultiplier
            val memberDishes = dishes.filter { it.assignedMemberIds.contains(member.id) }

            val memberBoxHeight = 80f + (memberDishes.size * 42f)
            val memberRect = RectF(cardRect.left + 40f, currentY, cardRect.right - 40f, currentY + memberBoxHeight)

            val memberBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#F8FAFC")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(memberRect, 18f, 18f, memberBgPaint)

            // Member Header
            val namePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F172A")
                textSize = 34f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.LEFT
            }
            val amountPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#059669")
                textSize = 38f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.RIGHT
            }
            canvas.drawText("${member.name}", memberRect.left + 25f, memberRect.top + 48f, namePaint)
            canvas.drawText("Rs. ${numberFormat.format(memberTotal.toLong())}", memberRect.right - 25f, memberRect.top + 48f, amountPaint)

            var dishY = memberRect.top + 92f
            val dishPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 26f
                textAlign = Paint.Align.LEFT
            }

            memberDishes.forEach { dish ->
                val splitCount = dish.assignedMemberIds.size
                val splitLabel = if (splitCount > 1) " (1/$splitCount shared)" else ""
                val dishText = "• ${dish.name}: Rs. ${numberFormat.format((dish.price / splitCount).toLong())}$splitLabel"
                canvas.drawText(dishText, memberRect.left + 35f, dishY, dishPaint)
                dishY += 40f
            }

            currentY += memberBoxHeight + 24f
        }

        currentY += 10f
        drawDashedLine(canvas, cardRect.left + 30f, cardRect.right - 30f, currentY)
        currentY += 60f

        // Grand Total Row
        val totalLabelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 40f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.LEFT
        }
        val totalValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E11D48")
            textSize = 46f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.RIGHT
        }
        canvas.drawText(if (isEn) "GRAND TOTAL" else "जम्मा कुल रकम", cardRect.left + 50f, currentY, totalLabelPaint)
        canvas.drawText("Rs. ${numberFormat.format((subtotalAll * totalMultiplier).toLong())}", cardRect.right - 50f, currentY, totalValPaint)

        // Footer
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("✓ Verified Itemized Split • Nepal Patro NepTools", width / 2f, cardRect.bottom - 40f, footerPaint)

        return saveBitmapToCache(context, bitmap, "itemized_split_receipt.png")
    }

    private fun drawDashedLine(canvas: Canvas, startX: Float, endX: Float, y: Float) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 3f
            style = Paint.Style.STROKE
            pathEffect = DashPathEffect(floatArrayOf(14f, 10f), 0f)
        }
        val path = Path().apply {
            moveTo(startX, y)
            lineTo(endX, y)
        }
        canvas.drawPath(path, paint)
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File {
        // Must live under cache/exports/ so FileProvider can resolve a URI for it.
        val file = File(ExportDirs.cacheExports(context), fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    fun shareReceiptImage(context: Context, imageFile: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
