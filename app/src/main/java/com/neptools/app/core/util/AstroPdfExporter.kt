package com.neptools.app.core.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.widget.Toast
import androidx.core.content.FileProvider
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.vedic.AshtakootaGunaMilan
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.astrology.vedic.NakshatraCalc
import com.neptools.app.core.calendar.NepaliNames
import java.io.File
import java.io.FileOutputStream
import java.time.format.DateTimeFormatter

object AstroPdfExporter {

    private const val PAGE_WIDTH = 595
    private const val PAGE_HEIGHT = 842

    fun exportKundaliPdf(
        context: Context,
        chart: NatalChart,
        isShare: Boolean = false,
        isEn: Boolean = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    ) {
        if (!isShare) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
            if (printManager != null) {
                val printAdapter = object : PrintDocumentAdapter() {
                    override fun onLayout(
                        oldAttributes: PrintAttributes?,
                        newAttributes: PrintAttributes,
                        cancellationSignal: CancellationSignal?,
                        callback: LayoutResultCallback,
                        extras: Bundle?
                    ) {
                        if (cancellationSignal?.isCanceled == true) {
                            callback.onLayoutCancelled()
                            return
                        }
                        val info = PrintDocumentInfo.Builder("NepTools_Kundali_${System.currentTimeMillis()}.pdf")
                            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                            .setPageCount(1)
                            .build()
                        callback.onLayoutFinished(info, true)
                    }

                    override fun onWrite(
                        pages: Array<out PageRange>?,
                        destination: ParcelFileDescriptor,
                        cancellationSignal: CancellationSignal?,
                        callback: WriteResultCallback
                    ) {
                        try {
                            val pdfDoc = PdfDocument()
                            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                            val page = pdfDoc.startPage(pageInfo)
                            drawKundaliDocument(page.canvas, chart, isEn)
                            pdfDoc.finishPage(page)

                            val output = FileOutputStream(destination.fileDescriptor)
                            pdfDoc.writeTo(output)
                            pdfDoc.close()
                            callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                        } catch (e: Exception) {
                            callback.onWriteFailed(e.message)
                        }
                    }
                }
                val docTitle = if (isEn) "NepTools Janma Kundali" else "नेपटूल्स जन्म कुण्डली"
                printManager.print(docTitle, printAdapter, PrintAttributes.Builder().build())
                return
            }
        }

        // Fallback or share
        try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            drawKundaliDocument(page.canvas, chart, isEn)
            pdfDoc.finishPage(page)

            val cacheDir = File(ExportDirs.cacheExports(context), "documents")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "NepTools_Kundali_${System.currentTimeMillis()}.pdf")
            val output = FileOutputStream(pdfFile)
            pdfDoc.writeTo(output)
            output.flush()
            output.close()
            pdfDoc.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = if (isShare) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, if (isEn) "NepTools Vedic Kundali Report" else "नेपटूल्स वैदिक जन्म कुण्डली प्रतिवेदन")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(Intent.createChooser(intent, if (isEn) "Open Kundali PDF" else "कुण्डली PDF खोल्नुहोस्").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    fun exportGunaMilanPdf(
        context: Context,
        milan: AshtakootaGunaMilan.MilanResult,
        boyDetails: String,
        girlDetails: String,
        isShare: Boolean = false,
        isEn: Boolean = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
    ) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (!isShare && printManager != null) {
            val printAdapter = object : PrintDocumentAdapter() {
                override fun onLayout(
                    oldAttributes: PrintAttributes?,
                    newAttributes: PrintAttributes?,
                    cancellationSignal: CancellationSignal?,
                    callback: LayoutResultCallback,
                    extras: Bundle?
                ) {
                    if (cancellationSignal?.isCanceled == true) {
                        callback.onLayoutCancelled()
                        return
                    }
                    val info = PrintDocumentInfo.Builder("NepTools_GunaMilan_${System.currentTimeMillis()}.pdf")
                        .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                        .setPageCount(1)
                        .build()
                    callback.onLayoutFinished(info, true)
                }

                override fun onWrite(
                    pages: Array<out PageRange>?,
                    destination: ParcelFileDescriptor,
                    cancellationSignal: CancellationSignal?,
                    callback: WriteResultCallback
                ) {
                    try {
                        val pdfDoc = PdfDocument()
                        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
                        val page = pdfDoc.startPage(pageInfo)
                        drawGunaMilanDocument(page.canvas, milan, boyDetails, girlDetails, isEn)
                        pdfDoc.finishPage(page)

                        val output = FileOutputStream(destination.fileDescriptor)
                        pdfDoc.writeTo(output)
                        pdfDoc.close()
                        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                    } catch (e: Exception) {
                        callback.onWriteFailed(e.message)
                    }
                }
            }
            printManager.print("NepTools Guna Milan Report", printAdapter, PrintAttributes.Builder().build())
            return
        }

        try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
            val page = pdfDoc.startPage(pageInfo)
            drawGunaMilanDocument(page.canvas, milan, boyDetails, girlDetails, isEn)
            pdfDoc.finishPage(page)

            val cacheDir = File(ExportDirs.cacheExports(context), "documents")
            if (!cacheDir.exists()) cacheDir.mkdirs()
            val pdfFile = File(cacheDir, "NepTools_GunaMilan_${System.currentTimeMillis()}.pdf")
            val output = FileOutputStream(pdfFile)
            pdfDoc.writeTo(output)
            output.flush()
            output.close()
            pdfDoc.close()

            val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", pdfFile)
            val intent = if (isShare) {
                Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "NepTools Guna Milan Compatibility Report")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            } else {
                Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            }
            context.startActivity(Intent.createChooser(intent, "Open Guna Milan PDF").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (e: Exception) {
            Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawKundaliDocument(canvas: Canvas, chart: NatalChart, isEn: Boolean = false) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // Outer border line
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(199, 62, 46) // Brand Vermilion
        paint.strokeWidth = 2f
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.strokeWidth = 0.75f
        canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), paint)

        // Header Subtitle
        paint.style = Paint.Style.FILL
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(199, 62, 46)
        val headerSubtitle = if (isEn) "NEPTOOLS • VEDIC ASTROLOGY SUITE" else "नेपटूल्स • वैदिक ज्योतिष सेवा"
        canvas.drawText(headerSubtitle, 42f, 48f, paint)

        // Title
        paint.textSize = 17f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val titleText = if (isEn) "Vedic Janma Kundali (Birth Chart)" else "वैदिक जन्म कुण्डली (लग्न चक्र)"
        canvas.drawText(titleText, 42f, 70f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(100, 116, 139)
        val subText = if (isEn) "Authentic High-Precision Nirayana Sidereal Astrological Chart" else "प्रामाणिक उच्च-सटीक निरयण ज्योतिषीय चक्र"
        canvas.drawText(subText, 42f, 85f, paint)

        // Birth Info Box
        paint.color = Color.rgb(248, 250, 252)
        val infoRect = RectF(40f, 100f, (PAGE_WIDTH - 40).toFloat(), 175f)
        canvas.drawRoundRect(infoRect, 8f, 8f, paint)

        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(infoRect, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 9.5f
        paint.color = Color.rgb(30, 41, 59)
        val b = chart.birth
        val dateLabel = if (isEn) "Date of Birth: ${b.date}" else "जन्म मिति: ${b.date}"
        val timeLabel = if (isEn) "Birth Time: ${b.time}" else "जन्म समय: ${b.time}"
        val placeLabel = if (isEn) "Place: ${b.placeLabel} (Lat: ${String.format("%.2f", b.latitude)}, Lon: ${String.format("%.2f", b.longitude)})"
                         else "स्थान: ${b.placeLabel} (अक्षांश: ${String.format("%.2f", b.latitude)}, देशान्तर: ${String.format("%.2f", b.longitude)})"

        canvas.drawText(dateLabel, 55f, 122f, paint)
        canvas.drawText(timeLabel, 55f, 140f, paint)
        canvas.drawText(placeLabel, 55f, 158f, paint)

        val lagnaSignName = if (isEn) Signs.en.getOrElse(chart.lagnaSign) { "Aries" } else Signs.np.getOrElse(chart.lagnaSign) { "मेष" }
        val lagnaNum = if (isEn) "${chart.lagnaSign + 1}" else NepaliNames.toDevanagari((chart.lagnaSign + 1).toString())
        val lagnaLabel = if (isEn) "Ascendant (Lagna): $lagnaSignName ($lagnaNum)" else "लग्न भाव: $lagnaSignName ($lagnaNum)"
        canvas.drawText(lagnaLabel, 320f, 122f, paint)

        val moonPos = chart.positions[Planet.MOON]
        val moonSignName = moonPos?.let { if (isEn) Signs.en.getOrElse(it.signIndex) { "Taurus" } else Signs.np.getOrElse(it.signIndex) { "वृष" } } ?: "-"
        val rashiLabel = if (isEn) "Moon Sign: $moonSignName" else "चन्द्र राशि: $moonSignName"
        canvas.drawText(rashiLabel, 320f, 140f, paint)

        val nakLabel = if (isEn) "Moon Star: ${chart.moonNakshatra}" else "चन्द्र नक्षत्र: ${chart.moonNakshatra}"
        canvas.drawText(nakLabel, 320f, 158f, paint)

        // Draw North Indian Diamond Kundali Chart
        val chartLeft = 110f
        val chartTop = 195f
        val chartSize = 375f
        drawNorthIndianDiamondChart(canvas, chart, chartLeft, chartTop, chartSize, isEn)

        // Planetary Summary Table
        var tableY = chartTop + chartSize + 30f
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val posTitle = if (isEn) "Planetary Positions (Nirayana Sidereal)" else "ग्रह स्थिति (निरयण पद्धति)"
        canvas.drawText(posTitle, 40f, tableY, paint)

        tableY += 15f
        paint.color = Color.rgb(241, 245, 249)
        canvas.drawRect(40f, tableY - 12f, (PAGE_WIDTH - 40).toFloat(), tableY + 6f, paint)

        paint.textSize = 9f
        paint.color = Color.rgb(71, 85, 105)
        val thPlanet = if (isEn) "Planet" else "ग्रह"
        val thSign = if (isEn) "Sign" else "राशि"
        val thDeg = if (isEn) "Degree" else "अंश"
        val thNak = if (isEn) "Nakshatra" else "नक्षत्र"
        val thPada = if (isEn) "Pada" else "चरण"
        val thHouse = if (isEn) "House" else "भाव"

        canvas.drawText(thPlanet, 50f, tableY, paint)
        canvas.drawText(thSign, 120f, tableY, paint)
        canvas.drawText(thDeg, 210f, tableY, paint)
        canvas.drawText(thNak, 290f, tableY, paint)
        canvas.drawText(thPada, 410f, tableY, paint)
        canvas.drawText(thHouse, 480f, tableY, paint)

        paint.typeface = Typeface.DEFAULT
        tableY += 18f

        val planetNamesNpMap = mapOf(
            Planet.SUN to "सूर्य",
            Planet.MOON to "चन्द्रमा",
            Planet.MARS to "मङ्गल",
            Planet.MERCURY to "बुध",
            Planet.JUPITER to "बृहस्पति",
            Planet.VENUS to "शुक्र",
            Planet.SATURN to "शनि",
            Planet.RAHU to "राहु",
            Planet.KETU to "केतु"
        )

        chart.positions.forEach { (planet, pos) ->
            val pName = if (isEn) planet.name.lowercase().replaceFirstChar { it.uppercase() } else planetNamesNpMap[planet] ?: planet.name
            val signName = if (isEn) Signs.en.getOrElse(pos.signIndex) { "-" } else Signs.np.getOrElse(pos.signIndex) { "-" }
            val deg = if (isEn) String.format("%.2f°", pos.degreeInSign) else "${NepaliNames.toDevanagari(String.format("%.2f", pos.degreeInSign))}°"
            val retro = if (pos.retrograde) (if (isEn) " (R)" else " (व)") else ""

            paint.color = Color.rgb(30, 41, 59)
            canvas.drawText("$pName$retro", 50f, tableY, paint)
            canvas.drawText(signName, 120f, tableY, paint)
            canvas.drawText(deg, 210f, tableY, paint)
            val nkName = if (isEn) NakshatraCalc.namesEn.getOrElse(NakshatraCalc.index(pos.siderealLon)) { pos.nakshatraName } else pos.nakshatraName
            canvas.drawText(nkName, 290f, tableY, paint)
            val padaStr = if (isEn) "${pos.nakshatraPada}" else NepaliNames.toDevanagari(pos.nakshatraPada.toString())
            canvas.drawText(padaStr, 410f, tableY, paint)
            val houseStr = if (isEn) "H${pos.houseFromLagna}" else "${NepaliNames.toDevanagari(pos.houseFromLagna.toString())}"
            canvas.drawText(houseStr, 480f, tableY, paint)

            tableY += 15f
        }

        // Footer
        paint.color = Color.rgb(148, 163, 184)
        paint.textSize = 8f
        canvas.drawLine(40f, (PAGE_HEIGHT - 35).toFloat(), (PAGE_WIDTH - 40).toFloat(), (PAGE_HEIGHT - 35).toFloat(), paint)
        val footerText = if (isEn) "Generated locally by NepTools • 100% Offline & Private • www.neptools.app"
                         else "नेपटूल्सद्वारा उपकरणमै निर्मित • १००% अफलाइन र सुरक्षित • www.neptools.app"
        canvas.drawText(footerText, 40f, (PAGE_HEIGHT - 22).toFloat(), paint)
    }

    private fun drawNorthIndianDiamondChart(
        canvas: Canvas,
        chart: NatalChart,
        left: Float,
        top: Float,
        size: Float,
        isEn: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val right = left + size
        val bottom = top + size
        val midX = left + size / 2f
        val midY = top + size / 2f

        // Outer square
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1.5f
        paint.color = Color.rgb(199, 62, 46)
        canvas.drawRect(left, top, right, bottom, paint)

        // Outer Diagonals
        canvas.drawLine(left, top, right, bottom, paint)
        canvas.drawLine(right, top, left, bottom, paint)

        // Inner Diamond
        canvas.drawLine(midX, top, right, midY, paint)
        canvas.drawLine(right, midY, midX, bottom, paint)
        canvas.drawLine(midX, bottom, left, midY, paint)
        canvas.drawLine(left, midY, midX, top, paint)

        // Map planets to houses
        val housePlanets = mutableMapOf<Int, MutableList<String>>()
        chart.positions.forEach { (planet, pos) ->
            val abbr = when (planet) {
                Planet.SUN -> if (isEn) "Su" else "सू"
                Planet.MOON -> if (isEn) "Mo" else "चं"
                Planet.MARS -> if (isEn) "Ma" else "मं"
                Planet.MERCURY -> if (isEn) "Me" else "बु"
                Planet.JUPITER -> if (isEn) "Ju" else "बृ"
                Planet.VENUS -> if (isEn) "Ve" else "शु"
                Planet.SATURN -> if (isEn) "Sa" else "श"
                Planet.RAHU -> if (isEn) "Ra" else "रा"
                Planet.KETU -> if (isEn) "Ke" else "के"
            }
            housePlanets.getOrPut(pos.houseFromLagna) { mutableListOf() }.add(abbr)
        }

        // Centers for 12 houses
        val houseCenters = listOf(
            Pair(midX, top + size * 0.22f),         // House 1 (top diamond)
            Pair(left + size * 0.25f, top + size * 0.12f), // House 2
            Pair(left + size * 0.12f, top + size * 0.25f), // House 3
            Pair(left + size * 0.22f, midY),         // House 4 (left diamond)
            Pair(left + size * 0.12f, top + size * 0.75f), // House 5
            Pair(left + size * 0.25f, top + size * 0.88f), // House 6
            Pair(midX, top + size * 0.78f),         // House 7 (bottom diamond)
            Pair(left + size * 0.75f, top + size * 0.88f), // House 8
            Pair(left + size * 0.88f, top + size * 0.75f), // House 9
            Pair(left + size * 0.78f, midY),         // House 10 (right diamond)
            Pair(left + size * 0.88f, top + size * 0.25f), // House 11
            Pair(left + size * 0.75f, top + size * 0.12f)  // House 12
        )

        paint.style = Paint.Style.FILL
        houseCenters.forEachIndexed { i, pt ->
            val hIndex = i + 1
            val signNum = ((chart.lagnaSign + i) % 12) + 1

            // Draw sign number
            paint.textSize = 8f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(148, 163, 184)
            val signNumStr = if (isEn) "$signNum" else NepaliNames.toDevanagari(signNum.toString())
            canvas.drawText(signNumStr, pt.first - 4f, pt.second - 8f, paint)

            // Draw planets in this house
            val pls = housePlanets[hIndex] ?: emptyList()
            if (pls.isNotEmpty()) {
                paint.textSize = 9f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.color = Color.rgb(15, 23, 42)
                canvas.drawText(pls.joinToString(" "), pt.first - (pls.size * 6f), pt.second + 6f, paint)
            }
        }
    }

    private fun drawGunaMilanDocument(
        canvas: Canvas,
        milan: AshtakootaGunaMilan.MilanResult,
        boyDetails: String,
        girlDetails: String,
        isEn: Boolean = false
    ) {
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // 1. Clean Background
        paint.color = Color.WHITE
        canvas.drawRect(0f, 0f, PAGE_WIDTH.toFloat(), PAGE_HEIGHT.toFloat(), paint)

        // 2. Classical Vedic Certificate Double Frame
        paint.style = Paint.Style.STROKE
        paint.color = Color.rgb(199, 62, 46) // Brand Vermilion
        paint.strokeWidth = 2f
        canvas.drawRect(24f, 24f, (PAGE_WIDTH - 24).toFloat(), (PAGE_HEIGHT - 24).toFloat(), paint)

        paint.color = Color.rgb(226, 232, 240) // Slate border inset
        paint.strokeWidth = 0.75f
        canvas.drawRect(28f, 28f, (PAGE_WIDTH - 28).toFloat(), (PAGE_HEIGHT - 28).toFloat(), paint)

        // 3. Header Title Banner
        paint.style = Paint.Style.FILL
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(199, 62, 46)
        val headerSubtitle = if (isEn) "NEPTOOLS • VEDIC ASTROLOGY & JYOTISH SUITE" else "नेपटूल्स • वैदिक ज्योतिष सेवा"
        canvas.drawText(headerSubtitle, 42f, 46f, paint)

        paint.textSize = 16.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val reportTitle = if (isEn) "Vedic Marriage Compatibility Report (36 Gunas)" else "विवाह अष्टकूट गुण मिलान प्रतिवेदन (३६ गुण)"
        canvas.drawText(reportTitle, 42f, 68f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(100, 116, 139)
        val reportSub = if (isEn) "Authentic Ashta Koota Compatibility & Comprehensive Dosha Assessment" else "प्रामाणिक अष्टकूट गणना तथा विस्तृत दोष विश्लेषण"
        canvas.drawText(reportSub, 42f, 83f, paint)

        // 4. Groom & Bride Profile Cards (Side by Side)
        val cardTop = 96f
        val cardHeight = 60f
        val cardWidth = 248f

        val boyParts = boyDetails.split("•")
        val boyRashi = boyParts.getOrNull(0)?.trim() ?: boyDetails
        val boyNak = boyParts.getOrNull(1)?.trim() ?: ""

        val girlParts = girlDetails.split("•")
        val girlRashi = girlParts.getOrNull(0)?.trim() ?: girlDetails
        val girlNak = girlParts.getOrNull(1)?.trim() ?: ""

        val groomLabel = if (isEn) "Groom:" else "वर:"
        val brideLabel = if (isEn) "Bride:" else "वधू:"
        val rashiLabel = if (isEn) "Rashi:" else "राशि:"
        val nakLabel = if (isEn) "Nakshatra:" else "नक्षत्र:"

        // Groom Card
        val groomRect = RectF(42f, cardTop, 42f + cardWidth, cardTop + cardHeight)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(groomRect, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(groomRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText(groomLabel, 52f, cardTop + 16f, paint)
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("$rashiLabel $boyRashi", 52f, cardTop + 33f, paint)
        if (boyNak.isNotEmpty()) {
            canvas.drawText("$nakLabel $boyNak", 52f, cardTop + 49f, paint)
        }

        // Bride Card
        val brideLeft = (PAGE_WIDTH - 42 - cardWidth).toFloat()
        val brideRect = RectF(brideLeft, cardTop, brideLeft + cardWidth, cardTop + cardHeight)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(brideRect, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(brideRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawText(brideLabel, brideLeft + 10f, cardTop + 16f, paint)
        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8.5f
        paint.color = Color.rgb(71, 85, 105)
        canvas.drawText("$rashiLabel $girlRashi", brideLeft + 10f, cardTop + 33f, paint)
        if (girlNak.isNotEmpty()) {
            canvas.drawText("$nakLabel $girlNak", brideLeft + 10f, cardTop + 49f, paint)
        }

        // 5. Overall Score & Verdict Banner
        val bannerTop = 166f
        val bannerHeight = 70f
        val bannerRect = RectF(42f, bannerTop, (PAGE_WIDTH - 42).toFloat(), bannerTop + bannerHeight)
        val (bannerBg, bannerBorder, bannerText) = when (milan.verdictLevel) {
            AshtakootaGunaMilan.VerdictLevel.EXCELLENT -> Triple(Color.rgb(236, 253, 245), Color.rgb(16, 185, 129), Color.rgb(4, 120, 87))
            AshtakootaGunaMilan.VerdictLevel.GOOD -> Triple(Color.rgb(240, 249, 255), Color.rgb(14, 165, 233), Color.rgb(2, 132, 199))
            AshtakootaGunaMilan.VerdictLevel.AVERAGE -> Triple(Color.rgb(254, 252, 232), Color.rgb(245, 158, 11), Color.rgb(180, 83, 9))
            AshtakootaGunaMilan.VerdictLevel.LOW -> Triple(Color.rgb(254, 242, 242), Color.rgb(239, 68, 68), Color.rgb(185, 28, 28))
        }

        paint.color = bannerBg
        canvas.drawRoundRect(bannerRect, 8f, 8f, paint)
        paint.color = bannerBorder
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(bannerRect, 8f, 8f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = bannerText
        val scoreStr = if (isEn) {
            "${milan.totalPoints} / 36"
        } else {
            val totalEarnedRaw = if (milan.totalPoints % 1.0 == 0.0) "${milan.totalPoints.toInt()}" else "${milan.totalPoints}"
            "${NepaliNames.toDevanagari(totalEarnedRaw)} / ३६"
        }
        canvas.drawText(scoreStr, 56f, bannerTop + 36f, paint)

        paint.textSize = 9.5f
        paint.typeface = Typeface.DEFAULT
        val pctLabel = if (isEn) {
            if (milan.percentage >= 50) "Compatible" else "Low Compatibility"
        } else {
            if (milan.percentage >= 50) "स्वीकार्य" else "न्यून"
        }
        val matchWord = if (isEn) "Match" else "अनुकूल"
        val pctStr = if (isEn) "${milan.percentage}% $matchWord • $pctLabel"
                     else "${NepaliNames.toDevanagari(milan.percentage.toString())}% $matchWord • $pctLabel"
        canvas.drawText(pctStr, 56f, bannerTop + 54f, paint)

        // Draw Verdict (Render only the user's active language)
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = bannerText
        val verdictText = if (isEn) milan.verdictEn else milan.verdictNp
        drawMultilineText(
            canvas = canvas,
            text = verdictText,
            x = 185f,
            y = bannerTop + 28f,
            width = 345f,
            paint = paint,
            lineHeight = 14f
        )

        // 6. Ashta Koota Detailed Table
        val tableLeft = 42f
        val tableRight = (PAGE_WIDTH - 42).toFloat()
        var tableY = 248f

        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val tableTitle = if (isEn) "Ashta Koota Points Breakdown" else "अष्टकूट प्राप्ताङ्क विवरण"
        canvas.drawText(tableTitle, tableLeft, tableY, paint)

        tableY += 8f
        // Header Row
        val headerH = 24f
        paint.color = Color.rgb(30, 41, 59)
        canvas.drawRect(tableLeft, tableY, tableRight, tableY + headerH, paint)

        val c1 = tableLeft + 78f
        val c2 = c1 + 88f
        val c3 = c2 + 88f
        val c4 = c3 + 40f
        val c5 = c4 + 44f
        val c6 = c5 + 83f

        paint.textSize = 8.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.WHITE

        val thKoota = if (isEn) "Koota" else "कूट"
        val thBoy = if (isEn) "Groom" else "वर गुण"
        val thGirl = if (isEn) "Bride" else "वधू गुण"
        val thMax = if (isEn) "Max" else "पूर्णाङ्क"
        val thEarned = if (isEn) "Earned" else "प्राप्ताङ्क"
        val thDomain = if (isEn) "Domain" else "क्षेत्र"
        val thStatus = if (isEn) "Status" else "स्थिति"

        drawClippedText(canvas, thKoota, tableLeft + 6f, tableY + 16f, tableLeft + 2f, tableY, c1 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thBoy, c1 + 6f, tableY + 16f, c1 + 2f, tableY, c2 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thGirl, c2 + 6f, tableY + 16f, c2 + 2f, tableY, c3 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thMax, c3 + 6f, tableY + 16f, c3 + 2f, tableY, c4 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thEarned, c4 + 6f, tableY + 16f, c4 + 2f, tableY, c5 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thDomain, c5 + 6f, tableY + 16f, c5 + 2f, tableY, c6 - 2f, tableY + headerH, paint)
        drawClippedText(canvas, thStatus, c6 + 6f, tableY + 16f, c6 + 2f, tableY, tableRight - 2f, tableY + headerH, paint)

        tableY += headerH

        val rowH = 23f
        paint.typeface = Typeface.DEFAULT

        milan.kootas.forEachIndexed { i, koota ->
            val rowBg = if (i % 2 == 0) Color.rgb(255, 255, 255) else Color.rgb(248, 250, 252)
            paint.color = rowBg
            paint.style = Paint.Style.FILL
            canvas.drawRect(tableLeft, tableY, tableRight, tableY + rowH, paint)

            // Cell border lines
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.5f
            canvas.drawRect(tableLeft, tableY, tableRight, tableY + rowH, paint)
            canvas.drawLine(c1, tableY, c1, tableY + rowH, paint)
            canvas.drawLine(c2, tableY, c2, tableY + rowH, paint)
            canvas.drawLine(c3, tableY, c3, tableY + rowH, paint)
            canvas.drawLine(c4, tableY, c4, tableY + rowH, paint)
            canvas.drawLine(c5, tableY, c5, tableY + rowH, paint)
            canvas.drawLine(c6, tableY, c6, tableY + rowH, paint)

            // Text
            paint.style = Paint.Style.FILL
            paint.textSize = 8.5f
            paint.color = Color.rgb(30, 41, 59)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val kootaName = if (isEn) koota.nameEn else koota.nameNp
            drawClippedText(canvas, kootaName, tableLeft + 6f, tableY + 15f, tableLeft + 2f, tableY, c1 - 2f, tableY + rowH, paint)

            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(71, 85, 105)
            val boyVal = if (isEn) koota.boyValueEn else koota.boyValueNp
            val girlVal = if (isEn) koota.girlValueEn else koota.girlValueNp
            drawClippedText(canvas, boyVal, c1 + 5f, tableY + 15f, c1 + 2f, tableY, c2 - 2f, tableY + rowH, paint)
            drawClippedText(canvas, girlVal, c2 + 5f, tableY + 15f, c2 + 2f, tableY, c3 - 2f, tableY + rowH, paint)

            val maxPointsStr = if (isEn) "${koota.maxPoints.toInt()}" else NepaliNames.toDevanagari(koota.maxPoints.toInt().toString())
            drawClippedText(canvas, maxPointsStr, c3 + 12f, tableY + 15f, c3 + 2f, tableY, c4 - 2f, tableY + rowH, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = if (koota.earnedPoints == koota.maxPoints) Color.rgb(5, 150, 105)
                         else if (koota.earnedPoints > 0) Color.rgb(14, 165, 233)
                         else Color.rgb(220, 38, 38)
            val earnedRaw = if (koota.earnedPoints % 1.0 == 0.0) "${koota.earnedPoints.toInt()}" else "${koota.earnedPoints}"
            val earnedFormatted = if (isEn) earnedRaw else NepaliNames.toDevanagari(earnedRaw)
            drawClippedText(canvas, earnedFormatted, c4 + 12f, tableY + 15f, c4 + 2f, tableY, c5 - 2f, tableY + rowH, paint)

            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(71, 85, 105)
            val domainLabel = if (isEn) {
                when (koota.category) {
                    "Mental" -> "Mental"
                    "Health" -> "Health"
                    "Prosperity" -> "Prosperity"
                    "Physical" -> "Physical"
                    else -> koota.category
                }
            } else {
                when (koota.category) {
                    "Mental" -> "मानसिक"
                    "Health" -> "स्वास्थ्य"
                    "Prosperity" -> "समृद्धि"
                    "Physical" -> "शारीरिक"
                    else -> koota.category
                }
            }
            drawClippedText(canvas, domainLabel, c5 + 6f, tableY + 15f, c5 + 2f, tableY, c6 - 2f, tableY + rowH, paint)

            val statusText = if (isEn) {
                if (koota.earnedPoints == koota.maxPoints) "Full"
                else if (koota.earnedPoints > 0) "Partial"
                else "Zero"
            } else {
                if (koota.earnedPoints == koota.maxPoints) "पूर्ण अनुकूल"
                else if (koota.earnedPoints > 0) "आंशिक"
                else "शून्य"
            }
            drawClippedText(canvas, statusText, c6 + 6f, tableY + 15f, c6 + 2f, tableY, tableRight - 2f, tableY + rowH, paint)

            tableY += rowH
        }

        // Total Row
        val totalH = 25f
        paint.color = Color.rgb(241, 245, 249)
        paint.style = Paint.Style.FILL
        canvas.drawRect(tableLeft, tableY, tableRight, tableY + totalH, paint)

        paint.color = Color.rgb(203, 213, 225)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRect(tableLeft, tableY, tableRight, tableY + totalH, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(15, 23, 42)
        val totalLabel = if (isEn) "Total Points Earned:" else "जम्मा प्राप्ताङ्क:"
        canvas.drawText(totalLabel, tableLeft + 10f, tableY + 16f, paint)
        val maxTotalStr = if (isEn) "36" else "३६"
        canvas.drawText(maxTotalStr, c3 + 12f, tableY + 16f, paint)
        val totalEarnedRaw = if (milan.totalPoints % 1.0 == 0.0) "${milan.totalPoints.toInt()}" else "${milan.totalPoints}"
        val totalEarnedFormatted = if (isEn) totalEarnedRaw else NepaliNames.toDevanagari(totalEarnedRaw)
        canvas.drawText(totalEarnedFormatted, c4 + 12f, tableY + 16f, paint)
        val totalMatchText = if (isEn) "${milan.percentage}% Compatible" else "${NepaliNames.toDevanagari(milan.percentage.toString())}% अनुकूल"
        canvas.drawText(totalMatchText, c5 + 6f, tableY + 16f, paint)

        tableY += totalH + 14f

        // 7. Dosha Assessment Box
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val doshaTitle = if (isEn) "Vedic Dosha Assessment" else "दोष तथा ग्रह स्थिति परीक्षण"
        canvas.drawText(doshaTitle, tableLeft, tableY, paint)

        tableY += 8f
        val doshaBoxH = 74f
        val doshaRect = RectF(tableLeft, tableY, tableRight, tableY + doshaBoxH)
        paint.color = Color.rgb(248, 250, 252)
        canvas.drawRoundRect(doshaRect, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(doshaRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8.5f
        paint.typeface = Typeface.DEFAULT

        // Nadi
        val nadiColor = if (milan.hasNadiDosha) Color.rgb(220, 38, 38) else Color.rgb(5, 150, 105)
        val nadiText = if (isEn) {
            if (milan.hasNadiDosha) "• Nadi Dosha: Present — Progeny and health sensitivity; Mahamrityunjaya Japa advised"
            else "• Nadi Dosha: Absent (Favorable) — Optimal health, longevity, and family vitality"
        } else {
            if (milan.hasNadiDosha) "• नाडी दोष: उपस्थित — स्वास्थ्य तथा सन्तान सम्बन्धी संवेदनशीलता; महामृत्युञ्जय जप वा स्वर्ण दान उपयोगी"
            else "• नाडी दोष: दोषमुक्त (शुभ) — उत्तम स्वास्थ्य, कुल वृद्धि तथा दीर्घायु"
        }
        paint.color = nadiColor
        canvas.drawText(nadiText, tableLeft + 12f, tableY + 19f, paint)

        // Bhakoot
        val bhakootColor = if (milan.hasBhakootDosha) Color.rgb(220, 38, 38) else Color.rgb(5, 150, 105)
        val bhakootText = if (isEn) {
            if (milan.hasBhakootDosha) "• Bhakoot Dosha: Present — Mutual understanding caution needed; cancelled if lords are friends"
            else "• Bhakoot Dosha: Absent (Favorable) — Strong marital harmony and family prosperity"
        } else {
            if (milan.hasBhakootDosha) "• भकूट दोष: उपस्थित — आपसी तालमेल र आर्थिक योजनामा सतर्कता; राशि स्वामी मित्रता भए परिहार हुन्छ"
            else "• भकूट दोष: दोषमुक्त (शुभ) — दाम्पत्य सौहार्द, पारिवारिक समृद्धि र आर्थिक उन्नति"
        }
        paint.color = bhakootColor
        canvas.drawText(bhakootText, tableLeft + 12f, tableY + 39f, paint)

        // Gana
        val ganaColor = if (milan.hasGanaDosha) Color.rgb(217, 119, 6) else Color.rgb(5, 150, 105)
        val ganaText = if (isEn) {
            if (milan.hasGanaDosha) "• Gana Dosha: Present — Temperament adjustment needed; resolved through mutual respect"
            else "• Gana Dosha: Absent (Favorable) — Harmonious temperaments and shared mindset"
        } else {
            if (milan.hasGanaDosha) "• गण दोष: उपस्थित — स्वभाव र जीवनशैलीमा समन्वय आवश्यक; परस्पर सम्मानले दोष शान्त हुन्छ"
            else "• गण दोष: दोषमुक्त (शुभ) — मानसिक सामञ्जस्यता र विचारमा अनुकूलता"
        }
        paint.color = ganaColor
        canvas.drawText(ganaText, tableLeft + 12f, tableY + 59f, paint)

        tableY += doshaBoxH + 14f

        // 8. Domain Harmony Cards (4 columns)
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(30, 41, 59)
        val domainTitle = if (isEn) "Life Domain Compatibility Breakdown" else "जीवन क्षेत्रगत सामञ्जस्यता"
        canvas.drawText(domainTitle, tableLeft, tableY, paint)

        tableY += 8f
        val d = milan.summaryByDomain
        val domainColW = (tableRight - tableLeft - 24f) / 4f

        val domains = if (isEn) listOf(
            Triple("Mental Harmony", "${d.mentalHarmony} / 11", "Maitri & Gana"),
            Triple("Health & Children", "${d.healthAndProgeny} / 11", "Nadi & Tara"),
            Triple("Family & Growth", "${d.familyAndProsperity} / 10", "Bhakoot & Vashya"),
            Triple("Physical Harmony", "${d.physicalCompatibility} / 4", "Yoni")
        ) else listOf(
            Triple("मानसिक मेल", "${d.mentalHarmony} / 11", "मैत्री र गण"),
            Triple("स्वास्थ्य-सन्तान", "${d.healthAndProgeny} / 11", "नाडी र तारा"),
            Triple("पारिवारिक लाभ", "${d.familyAndProsperity} / 10", "भकूट र वश्य"),
            Triple("शारीरिक सौहार्द", "${d.physicalCompatibility} / 4", "योनि")
        )

        domains.forEachIndexed { i, dom ->
            val domLeft = tableLeft + i * (domainColW + 8f)
            val domRect = RectF(domLeft, tableY, domLeft + domainColW, tableY + 46f)

            paint.color = Color.rgb(248, 250, 252)
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(domRect, 6f, 6f, paint)
            paint.color = Color.rgb(226, 232, 240)
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = 0.75f
            canvas.drawRoundRect(domRect, 6f, 6f, paint)

            paint.style = Paint.Style.FILL
            paint.textSize = 8.5f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.color = Color.rgb(30, 41, 59)
            canvas.drawText(dom.first, domLeft + 8f, tableY + 16f, paint)

            paint.textSize = 11f
            paint.color = Color.rgb(199, 62, 46)
            canvas.drawText(dom.second, domLeft + 8f, tableY + 31f, paint)

            paint.textSize = 7f
            paint.typeface = Typeface.DEFAULT
            paint.color = Color.rgb(100, 116, 139)
            canvas.drawText(dom.third, domLeft + 8f, tableY + 42f, paint)
        }

        tableY += 58f

        // 9. Astrological Guidance & Counsel Box
        val guideBoxH = 74f
        val guideRect = RectF(tableLeft, tableY, tableRight, tableY + guideBoxH)
        paint.color = Color.rgb(248, 250, 252)
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(guideRect, 6f, 6f, paint)
        paint.color = Color.rgb(226, 232, 240)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 1f
        canvas.drawRoundRect(guideRect, 6f, 6f, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 9.5f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.color = Color.rgb(199, 62, 46)
        val guideTitle = if (isEn) "Astrological Counsel & Guidelines:" else "ज्योतिषीय परामर्श तथा शास्त्रीय निर्देश:"
        canvas.drawText(guideTitle, tableLeft + 12f, tableY + 18f, paint)

        paint.typeface = Typeface.DEFAULT
        paint.textSize = 8f
        paint.color = Color.rgb(71, 85, 105)
        if (isEn) {
            canvas.drawText("• Out of 36 points, 18 or more is traditionally considered auspicious and recommended for marriage.", tableLeft + 12f, tableY + 34f, paint)
            canvas.drawText("• Even if certain doshas exist, friendship between rashi lords or benefic planetary positions can mitigate them.", tableLeft + 12f, tableY + 49f, paint)
            canvas.drawText("• Guna Milan is an astrological compass—mutual trust, shared values, and understanding form the true foundation.", tableLeft + 12f, tableY + 64f, paint)
        } else {
            canvas.drawText("• ३६ गुणमध्ये १८ वा सोभन्दा बढी गुण प्राप्त भएमा विवाहका लागि शुभ मानिन्छ। २८ भन्दा माथि अति उत्तम योग हो।", tableLeft + 12f, tableY + 34f, paint)
            canvas.drawText("• यदि कुनै दोष देखिए तापनि राशि स्वामीको मित्रता वा कुण्डलीमा शुभ ग्रहको स्थितिले धेरै दोषहरू स्वतः निष्प्रभावी हुन्छन्।", tableLeft + 12f, tableY + 49f, paint)
            canvas.drawText("• गुण मिलान मार्गदर्शन हो—पारस्परिक विश्वास, संस्कार र पारिवारिक समझदारी नै दाम्पत्य जीवनका मुख्य आधार हुन्।", tableLeft + 12f, tableY + 64f, paint)
        }

        // 10. Footer & Certification
        val footY = (PAGE_HEIGHT - 38).toFloat()
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 0.75f
        paint.color = Color.rgb(203, 213, 225)
        canvas.drawLine(tableLeft, footY, tableRight, footY, paint)

        paint.style = Paint.Style.FILL
        paint.textSize = 8f
        paint.typeface = Typeface.DEFAULT
        paint.color = Color.rgb(148, 163, 184)
        if (isEn) {
            canvas.drawText("Generated on-device by NepTools Vedic Astrology Engine • 100% Offline & Private • Sidereal Nirayana System", tableLeft, footY + 13f, paint)
            paint.textSize = 7.5f
            canvas.drawText("Based on Brihat Parashara Hora Shastra & Muhurta Chintamani Tradition • www.neptools.app", tableLeft, footY + 24f, paint)
        } else {
            canvas.drawText("नेपटूल्स वैदिक ज्योतिष इन्जिनद्वारा उपकरणमै निर्मित • १००% अफलाइन र सुरक्षित • निरयण पद्धति", tableLeft, footY + 13f, paint)
            paint.textSize = 7.5f
            canvas.drawText("वृहत् पराशर होरा शास्त्र तथा मुहूर्त चिन्तामणि परम्परामा आधारित • www.neptools.app", tableLeft, footY + 24f, paint)
        }
    }

    private fun drawMultilineText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        width: Float,
        paint: Paint,
        lineHeight: Float = 14f
    ): Float {
        val words = text.split(" ")
        var currentLine = ""
        var curY = y
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) > width) {
                canvas.drawText(currentLine, x, curY, paint)
                currentLine = word
                curY += lineHeight
            } else {
                currentLine = testLine
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine, x, curY, paint)
            curY += lineHeight
        }
        return curY
    }

    private fun drawClippedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        clipLeft: Float,
        clipTop: Float,
        clipRight: Float,
        clipBottom: Float,
        paint: Paint
    ) {
        canvas.save()
        canvas.clipRect(clipLeft, clipTop, clipRight, clipBottom)
        canvas.drawText(text, x, y, paint)
        canvas.restore()
    }
}
