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
import android.os.Build
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import com.neptools.app.core.calendar.Festival
import com.neptools.app.core.calendar.NepaliDate
import com.neptools.app.core.calendar.NepaliNames
import com.neptools.app.core.calendar.Panchang
import com.neptools.app.core.calendar.SolarCalc
import com.neptools.app.core.calendar.SolarDay
import com.neptools.app.core.data.PatroRepo
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDate

object PatroGraphicGenerator {

    /**
     * Creates an authentic, elegant 1080x1440 Daily Patro & Panchang card
     * in the Newari Ink / Rice Paper design system for sharing on WhatsApp, Viber, etc.
     */
    fun createDailyPatroCard(
        context: Context,
        date: NepaliDate,
        adDate: LocalDate,
        panchang: Panchang,
        solar: SolarDay,
        festivals: List<Festival>,
        isEn: Boolean
    ): File {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // 1. Background (Warm Rice Paper Tone)
        canvas.drawColor(Color.parseColor("#FBF9F5"))

        // Outer & Inner Accent Border
        val margin = 36f
        val outerRect = RectF(margin, margin, width - margin, height - margin)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2D9C8")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(outerRect, 28f, 28f, borderPaint)

        val innerRect = RectF(margin + 12f, margin + 12f, width - margin - 12f, height - margin - 12f)
        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFE8DB")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(innerRect, 20f, 20f, innerBorderPaint)

        // 2. Header Top Banner (Newari Slate Ribbon)
        val headerRect = RectF(innerRect.left, innerRect.top, innerRect.right, innerRect.top + 160f)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val headerPath = Path().apply {
            addRoundRect(
                headerRect,
                floatArrayOf(20f, 20f, 20f, 20f, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(headerPath, headerPaint)

        // Header Tag
        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 22f
            letterSpacing = 0.15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "DAILY NEPALI PATRO & PANCHANG" else "नेपाली दैनिक पात्रो तथा पञ्चाङ्ग",
            width / 2f,
            headerRect.top + 55f,
            tagPaint
        )

        // Header Date String
        val bsYearStr = if (isEn) "BS ${date.year}" else "वि.सं. ${NepaliNames.toDevanagari(date.year)}"
        val bsMonthStr = if (isEn) NepaliNames.monthsEn.getOrElse(date.month - 1) { "" }
        else NepaliNames.monthsNp.getOrElse(date.month - 1) { "" }
        val bsDayStr = if (isEn) "${date.day}" else NepaliNames.toDevanagari(date.day)
        val weekdayStr = if (isEn) NepaliNames.weekdaysEn[adDate.dayOfWeek.value % 7]
        else NepaliNames.weekdaysNp[adDate.dayOfWeek.value % 7]

        val headerDatePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "$bsYearStr $bsMonthStr $bsDayStr, $weekdayStr",
            width / 2f,
            headerRect.top + 115f,
            headerDatePaint
        )

        // 3. Gregorian Sub-Header
        val adMonthName = adDate.month.name.lowercase().replaceFirstChar { it.uppercase() }
        val adDateStr = "${adDate.dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() }}, $adMonthName ${adDate.dayOfMonth}, ${adDate.year}"
        val adTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(adDateStr, width / 2f, headerRect.bottom + 48f, adTextPaint)

        // 4. Hero Date Block
        val heroBoxTop = headerRect.bottom + 70f
        val heroBoxBottom = heroBoxTop + 250f
        val heroBoxRect = RectF(innerRect.left + 30f, heroBoxTop, innerRect.right - 30f, heroBoxBottom)
        val heroBoxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(16f, 0f, 6f, Color.argb(40, 0, 0, 0))
        }
        canvas.drawRoundRect(heroBoxRect, 20f, 20f, heroBoxPaint)

        val heroBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(heroBoxRect, 20f, 20f, heroBorderPaint)

        // Large Day Numeral
        val dayNumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 120f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(bsDayStr, width / 2f, heroBoxTop + 125f, dayNumPaint)

        // Paksha & Tithi
        val tName = if (isEn) panchang.tithiNameEn.ifBlank { panchang.tithiName } else panchang.tithiName
        val pName = if (isEn) panchang.pakshaEn.ifBlank { panchang.paksha } else panchang.paksha
        val tithiLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("$pName · $tName", width / 2f, heroBoxTop + 195f, tithiLinePaint)

        // 5. Festival / Special Observance Banner
        var currentY = heroBoxBottom + 32f
        val fest = festivals.firstOrNull()
        val festName = if (fest != null) {
            if (isEn) fest.nameEn.ifBlank { fest.nameNp } else fest.nameNp
        } else {
            if (isEn) "Auspicious Day • Daily Observance" else "शुभ दिन • दैनिक नित्यकर्म"
        }

        val festRect = RectF(innerRect.left + 30f, currentY, innerRect.right - 30f, currentY + 90f)
        val festBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (fest?.isPublicHoliday == true) Color.parseColor("#FEE2E2") else Color.parseColor("#CCFBF1")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(festRect, 14f, 14f, festBgPaint)

        val festBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (fest?.isPublicHoliday == true) Color.parseColor("#FCA5A5") else Color.parseColor("#99F6E4")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(festRect, 14f, 14f, festBorder)

        val festTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = if (fest?.isPublicHoliday == true) Color.parseColor("#991B1B") else Color.parseColor("#115E59")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(festName, width / 2f, currentY + 56f, festTextPaint)

        currentY += 122f

        // 6. 4-Grid Astronomical Panchang Details
        val gridWidth = (innerRect.width() - 80f) / 2f
        val gridHeight = 110f

        val nakName = if (isEn) panchang.nakshatraNameEn.ifBlank { panchang.nakshatraName } else panchang.nakshatraName
        val yogName = if (isEn) panchang.yogaNameEn.ifBlank { panchang.yogaName } else panchang.yogaName
        val sunriseStr = SolarCalc.formatTime(solar.sunrise, isEn, !isEn)
        val sunsetStr = SolarCalc.formatTime(solar.sunset, isEn, !isEn)
        val rahuStr = rahuTextLocal(solar, isEn)

        // Tile 1: Nakshatra
        drawPanchangTile(
            canvas,
            RectF(innerRect.left + 30f, currentY, innerRect.left + 30f + gridWidth, currentY + gridHeight),
            if (isEn) "NAKSHATRA" else "नक्षत्र",
            nakName
        )

        // Tile 2: Yoga
        drawPanchangTile(
            canvas,
            RectF(innerRect.right - 30f - gridWidth, currentY, innerRect.right - 30f, currentY + gridHeight),
            if (isEn) "YOGA" else "योग",
            yogName
        )

        currentY += gridHeight + 16f

        // Tile 3: Sunrise & Sunset
        drawPanchangTile(
            canvas,
            RectF(innerRect.left + 30f, currentY, innerRect.left + 30f + gridWidth, currentY + gridHeight),
            if (isEn) "SUNRISE / SUNSET" else "सूर्योदय / सूर्यास्त",
            "$sunriseStr / $sunsetStr"
        )

        // Tile 4: Rahu Kaal
        drawPanchangTile(
            canvas,
            RectF(innerRect.right - 30f - gridWidth, currentY, innerRect.right - 30f, currentY + gridHeight),
            if (isEn) "RAHU KAAL" else "राहु काल",
            rahuStr
        )

        currentY += gridHeight + 36f

        // 7. Subhashita / Vedic Blessing Card
        val quoteRect = RectF(innerRect.left + 30f, currentY, innerRect.right - 30f, currentY + 140f)
        val quoteBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRoundRect(quoteRect, 14f, 14f, quoteBgPaint)

        val quoteBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(quoteRect, 14f, 14f, quoteBorder)

        val quoteDevPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#334155")
            textSize = 25f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "सर्वे भवन्तु सुखिनः सर्वे सन्तु निरामयाः।",
            width / 2f,
            quoteRect.top + 50f,
            quoteDevPaint
        )
        canvas.drawText(
            "सर्वे भद्राणि पश्यन्तु मा कश्चिद्दुःखभाग्भवेत्॥",
            width / 2f,
            quoteRect.top + 85f,
            quoteDevPaint
        )

        val quoteMeaningPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 19f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "May all beings be happy, peaceful and enlightened." else "सबै प्राणी सुखी, निरोगी र कल्याणमय होउन्।",
            width / 2f,
            quoteRect.top + 118f,
            quoteMeaningPaint
        )

        // 8. Footer Watermark & Branding
        val footerY = innerRect.bottom - 45f
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NepTools", width / 2f, footerY - 26f, brandPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 21f
            letterSpacing = 0.05f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "100% Ad-Free • Privacy-First Nepali Toolkit" else "विज्ञापनरहित, गोपनीयता-केन्द्रित नेपाली डिजिटल औजारहरू",
            width / 2f,
            footerY + 2f,
            taglinePaint
        )

        return saveBitmapToCache(context, bitmap, "neptools_patro_${date.year}_${date.month}_${date.day}.png")
    }

    /**
     * Creates an authentic Daily Rashifal card in the Newari Ink / Rice Paper design system
     * for sharing horoscopes on WhatsApp, Viber, Instagram, etc.
     */
    fun createRashifalCard(
        context: Context,
        rashiNameNp: String,
        rashiNameEn: String,
        glyph: String,
        reading: String,
        luckyColor: String,
        luckyNo: String,
        goodHours: String,
        isEn: Boolean
    ): File {
        val width = 1080
        val height = 1440
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.parseColor("#FBF9F5"))

        val margin = 36f
        val outerRect = RectF(margin, margin, width - margin, height - margin)
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2D9C8")
            style = Paint.Style.STROKE
            strokeWidth = 3f
        }
        canvas.drawRoundRect(outerRect, 28f, 28f, borderPaint)

        val innerRect = RectF(margin + 12f, margin + 12f, width - margin - 12f, height - margin - 12f)
        val innerBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#EFE8DB")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(innerRect, 20f, 20f, innerBorderPaint)

        // Header Top Banner
        val headerRect = RectF(innerRect.left, innerRect.top, innerRect.right, innerRect.top + 160f)
        val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            style = Paint.Style.FILL
        }
        val headerPath = Path().apply {
            addRoundRect(
                headerRect,
                floatArrayOf(20f, 20f, 20f, 20f, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
        }
        canvas.drawPath(headerPath, headerPaint)

        val tagPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 22f
            letterSpacing = 0.15f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "DAILY VEDIC HOROSCOPE" else "दैनिक वैदिक राशिफल",
            width / 2f,
            headerRect.top + 55f,
            tagPaint
        )

        val headerTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 38f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "$glyph  $rashiNameEn" else "$glyph  $rashiNameNp ($rashiNameEn)",
            width / 2f,
            headerRect.top + 115f,
            headerTitlePaint
        )

        // Today's Date Banner
        val todayBs = PatroRepo.d.engine.today()
        val bsDateStr = if (isEn)
            "${NepaliNames.monthsEn[todayBs.month - 1]} ${todayBs.day}, ${todayBs.year} BS"
        else
            "वि.सं. ${NepaliNames.toDevanagari(todayBs.year)} ${NepaliNames.monthsNp[todayBs.month - 1]} ${NepaliNames.toDevanagari(todayBs.day)} गते"

        val datePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 26f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(bsDateStr, width / 2f, headerRect.bottom + 52f, datePaint)

        // Reading Content Card
        val cardTop = headerRect.bottom + 80f
        val cardRect = RectF(innerRect.left + 30f, cardTop, innerRect.right - 30f, cardTop + 540f)
        val cardBgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(16f, 0f, 6f, Color.argb(35, 0, 0, 0))
        }
        canvas.drawRoundRect(cardRect, 20f, 20f, cardBgPaint)

        val cardBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(cardRect, 20f, 20f, cardBorder)

        // Reading Title
        val readingTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F766E")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(
            if (isEn) "Today's Astrological Prediction" else "आजको राशिफल विश्लेषण",
            cardRect.left + 36f,
            cardTop + 60f,
            readingTitlePaint
        )

        // Multi-line reading text layout
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1E293B")
            textSize = 32f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val textWidth = (cardRect.width() - 72f).toInt()
        val staticLayout = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StaticLayout.Builder.obtain(reading, 0, reading.length, textPaint, textWidth)
                .setAlignment(Layout.Alignment.ALIGN_NORMAL)
                .setLineSpacing(14f, 1.25f)
                .setIncludePad(false)
                .build()
        } else {
            @Suppress("DEPRECATION")
            StaticLayout(reading, textPaint, textWidth, Layout.Alignment.ALIGN_NORMAL, 1.25f, 14f, false)
        }

        canvas.save()
        canvas.translate(cardRect.left + 36f, cardTop + 100f)
        staticLayout.draw(canvas)
        canvas.restore()

        // Lucky Highlights Row
        var currentY = cardRect.bottom + 36f
        val chipWidth = (innerRect.width() - 80f) / 3f
        val chipHeight = 110f

        drawPanchangTile(
            canvas,
            RectF(innerRect.left + 30f, currentY, innerRect.left + 30f + chipWidth, currentY + chipHeight),
            if (isEn) "LUCKY COLOR" else "शुभ रङ",
            luckyColor
        )

        drawPanchangTile(
            canvas,
            RectF(innerRect.left + 40f + chipWidth, currentY, innerRect.left + 40f + chipWidth * 2f, currentY + chipHeight),
            if (isEn) "LUCKY NUMBER" else "शुभ अङ्क",
            luckyNo
        )

        drawPanchangTile(
            canvas,
            RectF(innerRect.left + 50f + chipWidth * 2f, currentY, innerRect.right - 30f, currentY + chipHeight),
            if (isEn) "GOOD HOURS" else "शुभ समय",
            goodHours
        )

        // Footer Branding
        val footerY = innerRect.bottom - 45f
        val brandPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("NepTools", width / 2f, footerY - 26f, brandPaint)

        val taglinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 21f
            letterSpacing = 0.05f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            if (isEn) "100% Ad-Free • Privacy-First Nepali Toolkit" else "विज्ञापनरहित, गोपनीयता-केन्द्रित नेपाली डिजिटल औजारहरू",
            width / 2f,
            footerY + 2f,
            taglinePaint
        )

        return saveBitmapToCache(context, bitmap, "neptools_rashifal_${rashiNameEn.lowercase()}.png")
    }

    private fun drawPanchangTile(canvas: Canvas, rect: RectF, label: String, value: String) {
        val tileBg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
            setShadowLayer(8f, 0f, 3f, Color.argb(20, 0, 0, 0))
        }
        canvas.drawRoundRect(rect, 14f, 14f, tileBg)

        val tileBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            style = Paint.Style.STROKE
            strokeWidth = 1.5f
        }
        canvas.drawRoundRect(rect, 14f, 14f, tileBorder)

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#64748B")
            textSize = 19f
            letterSpacing = 0.08f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(label, rect.centerX(), rect.top + 38f, labelPaint)

        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 27f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(value, rect.centerX(), rect.top + 80f, valPaint)
    }

    private fun rahuTextLocal(s: SolarDay, isEn: Boolean): String {
        val start = s.rahuStart
        val end = s.rahuEnd
        return if (start != null && end != null) {
            val sStr = SolarCalc.formatTime(start, isEn, !isEn)
            val eStr = SolarCalc.formatTime(end, isEn, !isEn)
            "$sStr - $eStr"
        } else {
            if (isEn) "N/A" else "उपलब्ध छैन"
        }
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap, fileName: String): File {
        val file = File(ExportDirs.cacheExports(context), fileName)
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
        return file
    }

    /**
     * Launches the system sharing intent to share the card directly on WhatsApp, Viber, Instagram, etc.
     */
    fun shareCardImage(context: Context, imageFile: File, title: String) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            imageFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_TEXT, "$title\nShared via NepTools — 100% Ad-Free Nepali Toolkit\nhttps://github.com/shubhambelbase/NepTools")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, title))
    }
}
