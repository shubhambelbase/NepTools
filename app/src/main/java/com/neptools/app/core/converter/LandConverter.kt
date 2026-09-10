package com.neptools.app.core.converter

import kotlin.math.roundToInt

object LandConverter {
    // Nepali traditional land units - Terai/Pahad unified to sq meters
    // 1 Ropani = 16 Aana = 64 Paisa = 256 Dam = 508.74 sqm (Kathmandu standard)
    // 1 Bigha = 13.31 Ropani = 6772.63 sqm (Terai)
    // 1 Kattha = 338.63 sqm, 1 Dhur = 16.93 sqm (Bigha=20 Kattha, Kattha=20 Dhur)
    const val SQM_PER_ROPANI = 508.74
    const val SQM_PER_AANA = SQM_PER_ROPANI / 16.0
    const val SQM_PER_PAISA = SQM_PER_ROPANI / 64.0
    const val SQM_PER_DAM = SQM_PER_ROPANI / 256.0
    const val SQM_PER_BIGHA = 6772.63
    const val SQM_PER_KATTHA = 338.6316
    const val SQM_PER_DHUR = 16.93158
    const val SQM_PER_SQM = 1.0
    const val SQM_PER_SQFT = 0.092903
    const val SQM_PER_SQM_KM = 1_000_000.0
    const val SQM_PER_HECTARE = 10000.0
    const val SQM_PER_ACRE = 4046.86

    enum class Unit(val labelNp: String, val labelEn: String, val sqm: Double) {
        ROPANI("रोपनी", "Ropani", SQM_PER_ROPANI),
        AANA("आना", "Aana", SQM_PER_AANA),
        PAISA("पैसा", "Paisa", SQM_PER_PAISA),
        DAM("दाम", "Dam", SQM_PER_DAM),
        BIGHA("बिघा", "Bigha", SQM_PER_BIGHA),
        KATTHA("कट्ठा", "Kattha", SQM_PER_KATTHA),
        DHUR("धुर", "Dhur", SQM_PER_DHUR),
        SQM("वर्ग मिटर", "Sq Meter", SQM_PER_SQM),
        SQFT("वर्ग फिट", "Sq Feet", SQM_PER_SQFT),
        HECTARE("हेक्टर", "Hectare", SQM_PER_HECTARE),
        ACRE("एकड", "Acre", SQM_PER_ACRE),
    }

    data class RopaniBreakdown(val ropani: Int, val aana: Int, val paisa: Int, val dam: Double)

    fun toSqm(amount: Double, unit: Unit): Double = amount * unit.sqm

    fun fromSqm(sqm: Double, target: Unit): Double = sqm / target.sqm

    fun convert(amount: Double, from: Unit, to: Unit): Double = fromSqm(toSqm(amount, from), to)

    fun toRopaniBreakdown(totalSqm: Double): RopaniBreakdown {
        var rem = totalSqm
        val ropani = (rem / SQM_PER_ROPANI).toInt()
        rem -= ropani * SQM_PER_ROPANI
        val aana = (rem / SQM_PER_AANA).toInt()
        rem -= aana * SQM_PER_AANA
        val paisa = (rem / SQM_PER_PAISA).toInt()
        rem -= paisa * SQM_PER_PAISA
        val dam = rem / SQM_PER_DAM
        return RopaniBreakdown(ropani, aana, paisa, dam)
    }

    fun breakdownToSqm(ropani: Int, aana: Int, paisa: Int, dam: Double): Double =
        ropani * SQM_PER_ROPANI + aana * SQM_PER_AANA + paisa * SQM_PER_PAISA + dam * SQM_PER_DAM

    fun formatBreakdown(b: RopaniBreakdown): String {
        val damStr = if (b.dam < 0.005) "0" else String.format("%.2f", b.dam)
        return "${b.ropani}-${b.aana}-${b.paisa}-${damStr}"
    }

    fun formatBighaBreakdown(totalSqm: Double): String {
        var rem = totalSqm
        val bigha = (rem / SQM_PER_BIGHA).toInt()
        rem -= bigha * SQM_PER_BIGHA
        val kattha = (rem / SQM_PER_KATTHA).toInt()
        rem -= kattha * SQM_PER_KATTHA
        val dhur = rem / SQM_PER_DHUR
        return String.format("%d-%d-%.2f", bigha, kattha, dhur)
    }
}
