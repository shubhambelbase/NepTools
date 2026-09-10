package com.neptools.app.core.converter

enum class FileConverterMode { IMAGE_TO_PDF, PDF_TO_IMAGE }

enum class ConverterPageSize(val labelEn: String, val labelNp: String, val widthPt: Float, val heightPt: Float) {
    ORIGINAL("Original", "मौलिक", 0f, 0f),
    A4("A4", "A4", 595f, 842f),
    A5("A5", "A5", 420f, 595f),
    LETTER("Letter", "लेटर", 612f, 792f)
}

enum class PdfImageFormat(val label: String) { JPG("JPG"), PNG("PNG") }

data class PdfPageInfo(val index: Int, val widthPt: Float, val heightPt: Float)
