package com.neptools.app.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.addPathNodes as parse
import androidx.compose.ui.unit.dp

private fun icon(name: String, block: ImageVector.Builder.() -> Unit): ImageVector =
    ImageVector.Builder(
        name = name,
        defaultWidth = 24.dp,
        defaultHeight = 24.dp,
        viewportWidth = 24f,
        viewportHeight = 24f
    ).apply(block).build()

private fun ImageVector.Builder.sp(d: String, w: Float = 1.8f) {
    addPath(
        pathData = parse(d),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = w,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    )
}

object PIcons {
    val Swap = icon("Swap") { sp("M4 7h13l-3-3M20 17H7l3 3") }
    val Coin = icon("Coin") {
        sp("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z")
        sp("M9.5 8.5h5M9.5 12h5M12 8.5V16", 1.6f)
    }
    val Sparkle = icon("Sparkle") { sp("M12 3l2.2 6.3L21 11l-6.8 1.7L12 19l-2.2-6.3L3 11l6.8-1.7L12 3Z") }
    val Stars = icon("Stars") { sp("M12 3l2.7 6.9L22 11l-5.5 4.9L18 23l-6-3.8L6 23l1.5-7.1L2 11l7.3-1.1z") }
    val Cloud = icon("Cloud") { sp("M7 18a4 4 0 0 1-.4-7.98A5.5 5.5 0 0 1 17.3 9.6 3.8 3.8 0 0 1 17 18H7Z") }
    val Hourglass = icon("Hourglass") { sp("M7 3h10M7 21h10M8 3v3c0 2.5 4 4 4 6s-4 3.5-4 6v3M16 3v3c0 2.5-4 4-4 6s4 3.5 4 6v3") }
    val Ruler = icon("Ruler") { sp("M3 17 17 3l4 4L7 21l-4-4ZM8 12l2 2M11 9l2 2M14 6l2 2") }
    val Scan = icon("Scan") { sp("M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3M7 12h10") }
    val Mic = icon("Mic") { sp("M12 15a3 3 0 0 0 3-3V6a3 3 0 0 0-6 0v6a3 3 0 0 0 3 3ZM5.5 11.5A6.5 6.5 0 0 0 12 18a6.5 6.5 0 0 0 6.5-6.5M12 18v3") }
    val SunUp = icon("SunUp") { sp("M4 18h16M7.5 14a4.5 4.5 0 0 1 9 0M12 4v3M5.6 7.6 7 9M18.4 7.6 17 9") }
    val SunDown = icon("SunDown") { sp("M4 18h16M7.5 13a4.5 4.5 0 0 1 9 0M12 10V7M5.6 8.4 7 9.8M18.4 8.4 17 9.8") }
    val Bell = icon("Bell") { sp("M6 9a6 6 0 1 1 12 0c0 4 1.5 5.5 1.5 5.5h-15S6 13 6 9ZM10 19a2 2 0 0 0 4 0") }
    val Bus = icon("BusIc") { sp("M5 3h14a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2ZM4 12h16M8 21l1.5-3M16 21l-1.5-3M7.5 15.5h.01M16.5 15.5h.01", 1.7f) }
    val Lock = icon("Lock") { sp("M7 11h10a1 1 0 0 1 1 1v7a1 1 0 0 1-1 1H7a1 1 0 0 1-1-1v-7a1 1 0 0 1 1-1ZM8 11V8a4 4 0 0 1 8 0v3") }
    val Home = icon("HomeIc") { sp("M4 11 12 4l8 7v8a1 1 0 0 1-1 1h-4v-6H9v6H5a1 1 0 0 1-1-1v-8Z", 1.9f) }
    val Calendar = icon("CalendarIc") { sp("M5 5h14a1 1 0 0 1 1 1v13a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V6a1 1 0 0 1 1-1ZM8 3v4M16 3v4M4 10h16", 1.9f) }
    val Grid = icon("GridIc") { sp("M4 4h6v6H4zM14 4h6v6h-6zM4 14h6v6H4zM14 14h6v6h-6z", 1.9f) }
    val Gear = icon("GearIc") {
        sp("M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z")
        sp("M19.4 13.5a7.6 7.6 0 0 0 0-3l2-1.6-1.9-3.3-2.4 1a7.6 7.6 0 0 0-2.6-1.5L14 2.5h-4l-.5 2.6A7.6 7.6 0 0 0 6.9 6.6l-2.4-1-1.9 3.3 2 1.6a7.6 7.6 0 0 0 0 3l-2 1.6 1.9 3.3 2.4-1a7.6 7.6 0 0 0 2.6 1.5l.5 2.6h4l.5-2.6a7.6 7.6 0 0 0 2.6-1.5l2.4 1 1.9-3.3-2-1.6Z")
    }
    val ChevronLeft = icon("ChevL") { sp("M14 6l-6 6 6 6", 2.1f) }
    val ChevronRight = icon("ChevR") { sp("M10 6l6 6-6 6", 2.1f) }
    val ChevronDown = icon("ChevD") { sp("M6 9l6 6 6-6", 2.1f) }
    val ChevronUp = icon("ChevU") { sp("M18 15l-6-6-6 6", 2.1f) }
    val Info = icon("Info") {
        sp("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z")
        sp("M12 11v5M12 8h.01", 1.8f)
    }
    val CheckCircle = icon("CheckC") {
        sp("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18ZM8.5 12.5l2.5 2.5 4.5-5")
    }
    val Alert = icon("Alert") {
        sp("M12 9v4M12 17h.01M10.3 4.2 2.8 17a1.7 1.7 0 0 0 1.5 2.6h15.4a1.7 1.7 0 0 0 1.5-2.6L13.7 4.2a1.7 1.7 0 0 0-3 0Z")
    }
    val Search = icon("SearchIc") { sp("M11 18a7 7 0 1 0 0-14 7 7 0 0 0 0 14ZM20 20l-4-4") }
    val Pin = icon("PinIc") {
        sp("M12 21s7-5.1 7-11a7 7 0 1 0-14 0c0 5.9 7 11 7 11Z")
        sp("M12 12.5a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z", 1.6f)
    }
    val Phone = icon("PhoneIc") {
        sp("M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z")
    }
    val Zap = icon("ZapIc") { sp("M13 2L3 14h9l-1 8 10-12h-9l1-8z") }
    val Droplet = icon("DropletIc") { sp("M12 2.69l5.66 5.66a8 8 0 1 1-11.31 0z") }
    val Mail = icon("MailIc") { sp("M4 4h16c1.1 0 2 .9 2 2v12c0 1.1-.9 2-2 2H4c-1.1 0-2-.9-2-2V6c0-1.1.9-2 2-2zM22 6l-10 7L2 6") }
    val Fuel = icon("FuelIc") { sp("M3 22h12M4 9h10M4 5h10a2 2 0 0 1 2 2v15H2V7a2 2 0 0 1 2-2zM18 5l3 3v8a2 2 0 0 1-2 2h-1") }
    val Radio = icon("RadioIc") { sp("M4.9 19.1A10 10 0 0 1 4.9 4.9M7.8 16.2a6 6 0 0 1 0-8.5M12 12h.01M16.2 7.8a6 6 0 0 1 0 8.5M19.1 4.9a10 10 0 0 1 0 14.2") }
    val Play = icon("PlayIc") { sp("M5 3l14 9-14 9V3z") }
    val Pause = icon("PauseIc") { sp("M6 4h4v16H6zM14 4h4v16h-4z") }
    val QrCode = icon("QrCodeIc") { sp("M3 3h7v7H3zM14 3h7v7h-7zM3 14h7v7H3zM17 17h3v3h-3zM14 14h3v3h-3zM14 20h3v1h-3zM20 14h1v3h-1z") }
    val Copy = icon("CopyIc") { sp("M8 4v12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2V4a2 2 0 0 0-2-2h-8a2 2 0 0 0-2 2zM16 4h2a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2h2") }
    val Share = icon("ShareIc") { sp("M18 8a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM6 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM18 22a3 3 0 1 0 0-6 3 3 0 0 0 0 6ZM8.59 13.51l6.83 3.98M15.41 6.51l-6.82 3.98") }
    val Gold = icon("GoldIc") {
        sp("M6 16l3-8h6l3 8H6zM4 19h16M2 8l4-4h12l4 4")
    }
    val Leaf = icon("LeafIc") {
        sp("M11 20A7 7 0 0 1 4 13C4 6 11 3 20 3c0 9-3 16-9 17Z")
        sp("M11 13l9-9")
    }
    val Doc = icon("DocIc") {
        sp("M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6M16 13H8M16 17H8M10 9H8")
    }
    val Bank = icon("BankIc") {
        sp("M3 21h18M3 10h18M5 10v7M9 10v7M15 10v7M19 10v7M12 3l9 5H3l9-5z")
    }
    val Pdf = icon("PdfIc") {
        sp("M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8l-6-6z")
        sp("M14 2v6h6M9 13h2a1.5 1.5 0 0 0 0-3H9v6M15 16v-6h1.5a1.5 1.5 0 0 1 0 3H15")
    }
    val Thermometer = icon("ThermoIc") {
        sp("M14 14.76V3.5a2.5 2.5 0 0 0-5 0v11.26a4.5 4.5 0 1 0 5 0z")
    }
    val Sun = icon("SunIc") {
        sp("M12 1v2M12 21v2M4.22 4.22l1.42 1.42M18.36 18.36l1.42 1.42M1 12h2M21 12h2M4.22 19.78l1.42-1.42M18.36 5.64l1.42-1.42M12 17a5 5 0 1 0 0-10 5 5 0 0 0 0 10z")
    }
    val Car = icon("CarIc") {
        sp("M5 11l2-5h10l2 5M3 11h18v6H3zM6 17v2M18 17v2M7 14h.01M17 14h.01", 1.8f)
    }
    val Bike = icon("BikeIc") {
        sp("M5.5 17.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM18.5 17.5a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM5.5 14h5l3.5-5h4M12 17.5V14l-2.5-3.5", 1.8f)
    }
    val Bookmark = icon("BookmarkIc") {
        sp("M19 21l-7-5-7 5V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z", 1.8f)
    }
    val Award = icon("AwardIc") {
        sp("M12 15a6 6 0 1 0 0-12 6 6 0 0 0 0 12zM8.21 13.89L7 23l5-3 5 3-1.21-9.12", 1.8f)
    }
    val TrafficLight = icon("TrafficLightIc") {
        sp("M6 3h12a2 2 0 0 1 2 2v14a2 2 0 0 1-2 2H6a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2zM12 7h.01M12 12h.01M12 17h.01", 1.8f)
    }
    val Refresh = icon("RefreshIc") {
        sp("M23 4v6h-6M1 20v-6h6M3.51 9a9 9 0 0 1 14.85-3.36L23 10M1 14l4.64 4.36A9 9 0 0 0 20.49 15", 1.8f)
    }
    val BookOpen = icon("BookOpenIc") {
        sp("M2 3h6a4 4 0 0 1 4 4v14a3 3 0 0 0-3-3H2zM22 3h-6a4 4 0 0 0-4 4v14a3 3 0 0 1 3-3h7z", 1.8f)
    }
    val Timer = icon("TimerIc") {
        sp("M10 2h4M12 14l3-3M21 12a9 9 0 1 1-18 0 9 9 0 0 1 18 0z", 1.8f)
    }
    val Wrench = icon("WrenchIc") {
        sp("M14.7 6.3a1 1 0 0 0 0 1.4l1.6 1.6a1 1 0 0 0 1.4 0l3.77-3.77a6 6 0 0 1-7.94 7.94l-6.91 6.91a2.12 2.12 0 0 1-3-3l6.91-6.91a6 6 0 0 1 7.94-7.94l-3.76 3.76z", 1.8f)
    }
    val Shield = icon("ShieldIc") {
        sp("M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z", 1.8f)
    }
    val Check = icon("CheckIc") {
        sp("M20 6L9 17l-5-5", 2.2f)
    }
    val Cross = icon("CrossIc") {
        sp("M18 6L6 18M6 6l12 12", 2.2f)
    }
    val Lightbulb = icon("LightbulbIc") {
        sp("M9 18h6M10 22h4M15 14c.8-1 1.5-2.2 1.5-3.5A4.5 4.5 0 0 0 12 6a4.5 4.5 0 0 0-4.5 4.5c0 1.3.7 2.5 1.5 3.5", 1.8f)
    }
    val ImageCompress = icon("ImageCompressIc") {
        sp("M4 16l4-4 4 4 5-5 3 3M4 6h16a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2zM8 10a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM21 3l-4 4M3 21l4-4", 1.8f)
    }
    val WifiDrop = icon("WifiDropIc") {
        sp("M5 12.55a11 11 0 0 1 14.08 0M1.42 9a16 16 0 0 1 21.16 0M8.53 16.11a6 6 0 0 1 6.95 0M12 20h.01", 1.9f)
    }
    val Speedometer = icon("SpeedometerIc") {
        sp("M12 3a9 9 0 0 0-9 9c0 3.1 1.6 5.8 4 7.4M21 12a9 9 0 0 0-4-7.4M12 12l4-4M12 12a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM6 14h2M16 14h2M12 6v2", 1.8f)
    }
    val Download = icon("DownloadIc") {
        sp("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M7 10l5 5 5-5M12 15V3", 1.9f)
    }
    val Upload = icon("UploadIc") {
        sp("M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4M17 8l-5-5-5 5M12 3v12", 1.9f)
    }
    val Server = icon("ServerIc") {
        sp("M2 4h20v6H2zM2 14h20v6H2zM6 7h.01M6 17h.01", 1.8f)
    }
    val DecisionWheel = icon("DecisionWheelIc") {
        sp("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18z")
        sp("M12 3v18M3 12h18M5.64 5.64l12.72 12.72M5.64 18.36L18.36 5.64", 1.4f)
        sp("M12 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4z", 1.8f)
    }
    val Dice = icon("DiceIc") {
        sp("M4 4h16a2 2 0 0 1 2 2v12a2 2 0 0 1-2 2H4a2 2 0 0 1-2-2V6a2 2 0 0 1 2-2z", 1.9f)
        sp("M8 8h.01M16 8h.01M12 12h.01M8 16h.01M16 16h.01", 2.4f)
    }
    val Receipt = icon("ReceiptIc") {
        sp("M4 2v20l3-2 3 2 3-2 3 2 3-2 3 2V2l-3 2-3-2-3 2-3-2-3 2-3-2z", 1.8f)
        sp("M8 8h8M8 12h8M8 16h5", 1.8f)
    }
    val Users = icon("UsersIc") {
        sp("M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2")
        sp("M9 11a4 4 0 1 0 0-8 4 4 0 0 0 0 8z")
        sp("M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75")
    }
    val Whistle = icon("WhistleIc") {
        sp("M11 4a6 6 0 0 1 6 6v3h3a2 2 0 0 1 2 2v2a2 2 0 0 1-2 2h-9a6 6 0 1 1 0-12z")
        sp("M17 10h-3", 1.8f)
        sp("M6 16a2 2 0 1 0 0-4 2 2 0 0 0 0 4z", 1.4f)
    }
    val SoundWave = icon("SoundWaveIc") {
        sp("M2 10v4M6 6v12M10 3v18M14 8v8M18 5v14M22 10v4", 2.0f)
    }
    val CameraShare = icon("CameraShareIc") {
        sp("M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z")
        sp("M12 17a4 4 0 1 0 0-8 4 4 0 0 0 0 8z", 1.8f)
    }
    val MoreVert = icon("MoreVertIc") {
        sp("M12 6.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM12 13.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3zM12 20.5a1.5 1.5 0 1 0 0-3 1.5 1.5 0 0 0 0 3z", 2.2f)
    }
    val Compass = icon("CompassIc") {
        sp("M12 21a9 9 0 1 0 0-18 9 9 0 0 0 0 18Z", 1.9f)
        sp("M15.5 8.5l-1.8 4.7-4.7 1.8 1.8-4.7 4.7-1.8z", 1.6f)
    }
    val Level = icon("LevelIc") {
        sp("M3 8h18a1 1 0 0 1 1 1v6a1 1 0 0 1-1 1H3a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1Z", 1.8f)
        sp("M9 12a3 3 0 1 0 6 0 3 3 0 1 0-6 0", 1.6f)
    }
    val Eye = icon("EyeIc") {
        sp("M1 12s4-7 11-7 11 7 11 7-4 7-11 7S1 12 1 12Z")
        sp("M12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z", 1.6f)
    }
    val EyeOff = icon("EyeOffIc") {
        sp("M17.94 17.94A10.5 10.5 0 0 1 12 19c-7 0-11-7-11-7a18.5 18.5 0 0 1 5.06-5.06M9.9 4.24A9.5 9.5 0 0 1 12 5c7 0 11 7 11 7a18.5 18.5 0 0 1-2.16 3.19")
        sp("M14.12 14.12A3 3 0 1 1 9.88 9.88", 1.6f)
        sp("M1 1l22 22", 1.8f)
    }
    val Key = icon("KeyIc") {
        sp("M21 2l-2 2m-7.6 7.6a5.5 5.5 0 1 1-7.78 7.78 5.5 5.5 0 0 1 7.78-7.78Zm0 0L15.5 7.5m0 0 3 3L22 7l-3-3")
    }
    val Trash = icon("TrashIc") {
        sp("M3 6h18M8 6V4a1 1 0 0 1 1-1h6a1 1 0 0 1 1 1v2M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6M10 11v6M14 11v6", 1.8f)
    }
    val Plus = icon("PlusIc") { sp("M12 5v14M5 12h14", 2.1f) }
    val Fingerprint = icon("FingerprintIc") {
        sp("M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4", 1.6f)
        sp("M14 13.12c0 2.38 0 6.38-1 8.88", 1.6f)
        sp("M17.29 21.02c.12-.6.43-2.3.5-3.02", 1.6f)
        sp("M2 12a10 10 0 0 1 18-6", 1.7f)
        sp("M2 16h.01", 1.9f)
        sp("M21.8 16c.2-2 .13-5.35 0-6", 1.7f)
        sp("M5 19.5C5.5 18 6 15 6 12a6 6 0 0 1 .34-2", 1.6f)
        sp("M8.65 22c.21-.66.45-1.32.57-2", 1.6f)
        sp("M9 6.8a6 6 0 0 1 9 5.2v2", 1.6f)
    }
    val FileConvert = icon("FileConvertIc") {
        sp("M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8zM14 2v6h6", 1.8f)
        sp("M8 13h8M8 17h8", 1.6f)
        sp("M16 8l4 4M20 12l-4 4", 1.6f)
    }
    val Flame = icon("FlameIc") {
        sp("M8.5 14.5A2.5 2.5 0 0 0 11 12c0-1.38-.5-2-1-3-1.072-2.143-.224-4.054 2-6 .5 2.5 2 4.9 4 6.5 2 1.6 3 3.5 3 5.5a7 7 0 1 1-14 0c0-1.153.433-2.294 1-3a2.5 2.5 0 0 0 2.5 3z", 1.8f)
    }
    val Target = icon("TargetIc") {
        sp("M12 22a10 10 0 1 0 0-20 10 10 0 0 0 0 20Z", 1.8f)
        sp("M12 18a6 6 0 1 0 0-12 6 6 0 0 0 0 12Z", 1.6f)
        sp("M12 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4Z", 1.6f)
    }
    val Trophy = icon("TrophyIc") {
        sp("M6 9H4.5a2.5 2.5 0 0 1 0-5H6M18 9h1.5a2.5 2.5 0 0 0 0-5H18M6 4h12v6a6 6 0 0 1-12 0V4ZM12 16v3M8 22h8", 1.8f)
    }
    val TrendingUp = icon("TrendingUpIc") {
        sp("M23 6l-9.5 9.5-5-5L1 18M17 6h6v6", 2.0f)
    }
    val Edit = icon("EditIc") {
        sp("M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z", 1.8f)
    }
    val CreditCard = icon("CreditCardIc") {
        sp("M2 5h20a2 2 0 0 1 2 2v10a2 2 0 0 1-2 2H2a2 2 0 0 1-2-2V7a2 2 0 0 1 2-2zM2 10h20M6 14h3", 1.8f)
    }
    val Menu = icon("MenuIc") {
        sp("M4 6h16M4 12h16M4 18h16", 2.0f)
    }
    val SoundMeter = icon("SoundMeterIc") {
        sp("M11 5L6 9H2v6h4l5 4V5zM15.54 8.46a5 5 0 0 1 0 7.07M19.07 4.93a10 10 0 0 1 0 14.14", 1.8f)
    }
    val CameraSpy = icon("CameraSpyIc") {
        sp("M23 19a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h4l2-3h6l2 3h4a2 2 0 0 1 2 2z", 1.8f)
        sp("M12 16a3.5 3.5 0 1 0 0-7 3.5 3.5 0 0 0 0 7zM12 11v2M11 12h2", 1.6f)
    }
    val Bluetooth = icon("BluetoothIc") {
        sp("M6.5 6.5l11 11L12 23V1l5.5 5.5-11 11", 1.9f)
    }
    val Radar = icon("RadarIc") {
        sp("M12 22a10 10 0 1 0-10-10M12 18a6 6 0 1 0-6-6M12 14a2 2 0 1 0-2-2M12 12l7-7", 1.8f)
    }
    val Star = icon("StarIc") {
        sp("M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z", 1.8f)
    }
    val StarFilled = icon("StarFilledIc") {
        addPath(
            pathData = parse("M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z"),
            fill = SolidColor(Color(0xFFEAB308)),
            stroke = SolidColor(Color(0xFFCA8A04)),
            strokeLineWidth = 1f,
            strokeLineCap = StrokeCap.Round,
            strokeLineJoin = StrokeJoin.Round
        )
    }
}

