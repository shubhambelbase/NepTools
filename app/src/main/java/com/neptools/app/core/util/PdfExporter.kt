package com.neptools.app.core.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.io.FileInputStream

object PdfExporter {

    /**
     * Uses Android PrintManager to provide official system "Save as PDF" & "Print" dialog
     */
    fun printDocument(context: Context, title: String, bodyText: String) {
        val printManager = context.getSystemService(Context.PRINT_SERVICE) as? PrintManager
        if (printManager == null) {
            generateAndOpenPdf(context, title, bodyText, isShare = false)
            return
        }

        val printAdapter = object : PrintDocumentAdapter() {
            private var pdfDoc: PdfDocument? = null
            private var pageHeight = 842
            private var pageWidth = 595

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

                val pInfo = PrintDocumentInfo.Builder("Nepali_Application_${System.currentTimeMillis()}.pdf")
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                    .build()

                callback.onLayoutFinished(pInfo, true)
            }

            override fun onWrite(
                pages: Array<out PageRange>?,
                destination: ParcelFileDescriptor,
                cancellationSignal: CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    val document = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
                    val page = document.startPage(pageInfo)
                    val canvas = page.canvas

                    drawLetterPage(canvas, title, bodyText)
                    document.finishPage(page)

                    val outputStream = FileOutputStream(destination.fileDescriptor)
                    document.writeTo(outputStream)
                    document.close()

                    callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
                } catch (e: Exception) {
                    e.printStackTrace()
                    callback.onWriteFailed(e.message)
                }
            }
        }

        printManager.print(title, printAdapter, PrintAttributes.Builder().build())
    }

    fun generateAndOpenPdf(
        context: Context,
        title: String,
        bodyText: String,
        isShare: Boolean = false
    ) {
        try {
            val pdfDoc = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
            val page = pdfDoc.startPage(pageInfo)
            val canvas = page.canvas

            drawLetterPage(canvas, title, bodyText)
            pdfDoc.finishPage(page)

            // Write to Cache directory
            val cacheDir = File(context.cacheDir, "documents")
            if (!cacheDir.exists()) cacheDir.mkdirs()

            val pdfFile = File(cacheDir, "Nepali_Application_${System.currentTimeMillis()}.pdf")
            val outputStream = FileOutputStream(pdfFile)
            pdfDoc.writeTo(outputStream)
            outputStream.flush()
            outputStream.close()
            pdfDoc.close()

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                pdfFile
            )

            if (isShare) {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, title)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                val chooser = Intent.createChooser(shareIntent, "Share PDF / Share Document").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            } else {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/pdf")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                val chooser = Intent.createChooser(viewIntent, "Open PDF with...").apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
            }

            Toast.makeText(context, "PDF successfully generated! 📄", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error creating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawLetterPage(canvas: Canvas, title: String, bodyText: String) {
        // Page Background
        canvas.drawColor(Color.WHITE)

        // Outer Border
        val borderPaint = Paint().apply {
            color = Color.rgb(200, 200, 200)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(36f, 36f, 559f, 806f, borderPaint)

        // Title Paint
        val titlePaint = Paint().apply {
            color = Color.rgb(30, 40, 50)
            textSize = 15f
            typeface = Typeface.create(Typeface.SERIF, Typeface.BOLD)
            isAntiAlias = true
        }

        // Subtitle
        val subPaint = Paint().apply {
            color = Color.rgb(120, 130, 140)
            textSize = 9f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        // Draw Top Document Title
        canvas.drawText("नेपाल पात्रो — $title", 54f, 64f, titlePaint)
        canvas.drawText("Official Nepali Application Document · Format Approved", 54f, 78f, subPaint)

        // Separator
        canvas.drawLine(54f, 88f, 541f, 88f, borderPaint)

        // Body Text Layout
        val textPaint = TextPaint().apply {
            color = Color.rgb(25, 25, 25)
            textSize = 12f
            typeface = Typeface.create(Typeface.SERIF, Typeface.NORMAL)
            isAntiAlias = true
        }

        val textWidth = 487 // 541 - 54

        val staticLayout = StaticLayout.Builder
            .obtain(bodyText, 0, bodyText.length, textPaint, textWidth)
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setLineSpacing(4f, 1.2f)
            .setIncludePad(false)
            .build()

        canvas.save()
        canvas.translate(54f, 105f)
        staticLayout.draw(canvas)
        canvas.restore()

        // Footer
        val footerPaint = Paint().apply {
            color = Color.rgb(150, 150, 150)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.ITALIC)
            isAntiAlias = true
        }
        canvas.drawText("Generated via Nepal Patro (नेपाल पात्रो)", 54f, 792f, footerPaint)
    }
}
