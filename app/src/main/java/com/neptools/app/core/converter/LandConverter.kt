package com.neptools.app.core.converter

import androidx.compose.runtime.Immutable
import kotlin.math.abs
import kotlin.math.roundToLong

object LandConverter {

    // Official Nepal Survey Department (नापी विभाग) Land Measurement Standards
    // Hilly / Valley System:
    // 1 Ropani = 16 Aana = 64 Paisa = 256 Daam = 5476.0 sq ft = 508.73708 sq m
    const val SQFT_PER_ROPANI = 5476.0
    const val SQFT_PER_AANA = 342.25
    const val SQFT_PER_PAISA = 85.5625
    const val SQFT_PER_DAAM = 21.390625

    // Terai System:
    // 1 Bigha = 20 Katha = 400 Dhur = 1600 Kanwa = 72900.0 sq ft = 6772.6316 sq m
    const val SQFT_PER_BIGHA = 72900.0
    const val SQFT_PER_KATHA = 3645.0
    const val SQFT_PER_DHUR = 182.25
    const val SQFT_PER_KANWA = 45.5625

    // Metric and Imperial constants
    const val SQFT_PER_SQM = 10.7639104
    const val SQFT_PER_ACRE = 43560.0
    const val SQFT_PER_HECTARE = 107639.104

    @Immutable
    data class RopaniBreakdown(
        val ropani: Int,
        val aana: Int,
        val paisa: Int,
        val daam: Double
    ) {
        val totalSqFt: Double
            get() = ropani * SQFT_PER_ROPANI +
                    aana * SQFT_PER_AANA +
                    paisa * SQFT_PER_PAISA +
                    daam * SQFT_PER_DAAM

        val totalDaam: Double
            get() = ropani * 256.0 + aana * 16.0 + paisa * 4.0 + daam

        fun formatCompact(): String {
            val dStr = if (daam % 1.0 == 0.0) daam.toInt().toString() else "%.2f".format(daam)
            return "$ropani-$aana-$paisa-$dStr"
        }

        fun formatDescriptive(isEn: Boolean): String {
            val dStr = if (daam % 1.0 == 0.0) daam.toInt().toString() else "%.2f".format(daam)
            return if (isEn) {
                "$ropani Ropani, $aana Aana, $paisa Paisa, $dStr Daam"
            } else {
                "$ropani रोपनी, $aana आना, $paisa पैसा, $dStr दाम"
            }
        }
    }

    @Immutable
    data class BighaBreakdown(
        val bigha: Int,
        val katha: Int,
        val dhur: Int,
        val kanwa: Double
    ) {
        val totalSqFt: Double
            get() = bigha * SQFT_PER_BIGHA +
                    katha * SQFT_PER_KATHA +
                    dhur * SQFT_PER_DHUR +
                    kanwa * SQFT_PER_KANWA

        val totalKanwa: Double
            get() = bigha * 1600.0 + katha * 80.0 + dhur * 4.0 + kanwa

        fun formatCompact(): String {
            val kStr = if (kanwa % 1.0 == 0.0) kanwa.toInt().toString() else "%.2f".format(kanwa)
            return "$bigha-$katha-$dhur-$kStr"
        }

        fun formatDescriptive(isEn: Boolean): String {
            val kStr = if (kanwa % 1.0 == 0.0) kanwa.toInt().toString() else "%.2f".format(kanwa)
            return if (isEn) {
                "$bigha Bigha, $katha Katha, $dhur Dhur, $kStr Kanwa"
            } else {
                "$bigha बिघा, $katha कठ्ठा, $dhur धुर, $kStr कन्वा"
            }
        }
    }

    @Immutable
    data class FullLandCalculation(
        val sqFt: Double,
        val sqMeters: Double,
        val ropaniBreakdown: RopaniBreakdown,
        val bighaBreakdown: BighaBreakdown,
        val acres: Double,
        val hectares: Double
    )

    fun calculateFromSqFt(sqFt: Double): FullLandCalculation {
        val safeSqFt = if (sqFt < 0) 0.0 else sqFt
        val sqMeters = safeSqFt / SQFT_PER_SQM
        val acres = safeSqFt / SQFT_PER_ACRE
        val hectares = safeSqFt / SQFT_PER_HECTARE

        // Hilly breakdown
        var remSqFt = safeSqFt
        val ropani = (remSqFt / SQFT_PER_ROPANI).toInt()
        remSqFt -= ropani * SQFT_PER_ROPANI

        val aana = (remSqFt / SQFT_PER_AANA).toInt()
        remSqFt -= aana * SQFT_PER_AANA

        val paisa = (remSqFt / SQFT_PER_PAISA).toInt()
        remSqFt -= paisa * SQFT_PER_PAISA

        val daam = remSqFt / SQFT_PER_DAAM
        val ropaniB = RopaniBreakdown(ropani, aana, paisa, (daam * 100).roundToLong() / 100.0)

        // Terai breakdown
        remSqFt = safeSqFt
        val bigha = (remSqFt / SQFT_PER_BIGHA).toInt()
        remSqFt -= bigha * SQFT_PER_BIGHA

        val katha = (remSqFt / SQFT_PER_KATHA).toInt()
        remSqFt -= katha * SQFT_PER_KATHA

        val dhur = (remSqFt / SQFT_PER_DHUR).toInt()
        remSqFt -= dhur * SQFT_PER_DHUR

        val kanwa = remSqFt / SQFT_PER_KANWA
        val bighaB = BighaBreakdown(bigha, katha, dhur, (kanwa * 100).roundToLong() / 100.0)

        return FullLandCalculation(
            sqFt = safeSqFt,
            sqMeters = sqMeters,
            ropaniBreakdown = ropaniB,
            bighaBreakdown = bighaB,
            acres = acres,
            hectares = hectares
        )
    }

    fun ropaniToSqFt(ropani: Int, aana: Int, paisa: Int, daam: Double): Double =
        ropani * SQFT_PER_ROPANI + aana * SQFT_PER_AANA + paisa * SQFT_PER_PAISA + daam * SQFT_PER_DAAM

    fun bighaToSqFt(bigha: Int, katha: Int, dhur: Int, kanwa: Double): Double =
        bigha * SQFT_PER_BIGHA + katha * SQFT_PER_KATHA + dhur * SQFT_PER_DHUR + kanwa * SQFT_PER_KANWA

    fun sqMetersToSqFt(sqMeters: Double): Double = sqMeters * SQFT_PER_SQM

    // Arithmetic for Land Parcels (Carry and Borrow)
    fun addRopani(parcels: List<RopaniBreakdown>): RopaniBreakdown {
        var totalDaam = 0.0
        for (p in parcels) {
            totalDaam += p.totalDaam
        }
        val ropani = (totalDaam / 256.0).toInt()
        var rem = totalDaam - ropani * 256.0

        val aana = (rem / 16.0).toInt()
        rem -= aana * 16.0

        val paisa = (rem / 4.0).toInt()
        rem -= paisa * 4.0

        val daam = (rem * 100).roundToLong() / 100.0
        return RopaniBreakdown(ropani, aana, paisa, daam)
    }

    fun subtractRopani(base: RopaniBreakdown, subtract: RopaniBreakdown): RopaniBreakdown {
        val diffDaam = base.totalDaam - subtract.totalDaam
        val safeDaam = if (diffDaam < 0.0) 0.0 else diffDaam

        val ropani = (safeDaam / 256.0).toInt()
        var rem = safeDaam - ropani * 256.0

        val aana = (rem / 16.0).toInt()
        rem -= aana * 16.0

        val paisa = (rem / 4.0).toInt()
        rem -= paisa * 4.0

        val daam = (rem * 100).roundToLong() / 100.0
        return RopaniBreakdown(ropani, aana, paisa, daam)
    }

    fun addBigha(parcels: List<BighaBreakdown>): BighaBreakdown {
        var totalKanwa = 0.0
        for (p in parcels) {
            totalKanwa += p.totalKanwa
        }
        val bigha = (totalKanwa / 1600.0).toInt()
        var rem = totalKanwa - bigha * 1600.0

        val katha = (rem / 80.0).toInt()
        rem -= katha * 80.0

        val dhur = (rem / 4.0).toInt()
        rem -= dhur * 4.0

        val kanwa = (rem * 100).roundToLong() / 100.0
        return BighaBreakdown(bigha, katha, dhur, kanwa)
    }

    fun subtractBigha(base: BighaBreakdown, subtract: BighaBreakdown): BighaBreakdown {
        val diffKanwa = base.totalKanwa - subtract.totalKanwa
        val safeKanwa = if (diffKanwa < 0.0) 0.0 else diffKanwa

        val bigha = (safeKanwa / 1600.0).toInt()
        var rem = safeKanwa - bigha * 1600.0

        val katha = (rem / 80.0).toInt()
        rem -= katha * 80.0

        val dhur = (rem / 4.0).toInt()
        rem -= dhur * 4.0

        val kanwa = (rem * 100).roundToLong() / 100.0
        return BighaBreakdown(bigha, katha, dhur, kanwa)
    }

    // Legacy compatibility for ConverterScreen.kt
    enum class Unit(val labelNp: String, val labelEn: String, val sqm: Double) {
        ROPANI("रोपनी", "Ropani", 508.73708),
        AANA("आना", "Aana", 508.73708 / 16.0),
        PAISA("पैसा", "Paisa", 508.73708 / 64.0),
        DAM("दाम", "Dam", 508.73708 / 256.0),
        BIGHA("बिघा", "Bigha", 6772.6316),
        KATTHA("कट्ठा", "Kattha", 338.63158),
        DHUR("धुर", "Dhur", 16.93158),
        SQM("वर्ग मिटर", "Sq Meter", 1.0),
        SQFT("वर्ग फिट", "Sq Feet", 0.092903),
        HECTARE("हेक्टर", "Hectare", 10000.0),
        ACRE("एकड", "Acre", 4046.8564),
    }

    data class LegacyRopaniBreakdown(val ropani: Int, val aana: Int, val paisa: Int, val dam: Double)

    fun toSqm(amount: Double, unit: Unit): Double = amount * unit.sqm

    fun fromSqm(sqm: Double, target: Unit): Double = sqm / target.sqm

    fun convert(amount: Double, from: Unit, to: Unit): Double = fromSqm(toSqm(amount, from), to)

    fun toRopaniBreakdown(totalSqm: Double): LegacyRopaniBreakdown {
        var rem = totalSqm
        val ropani = (rem / 508.73708).toInt()
        rem -= ropani * 508.73708
        val aana = (rem / (508.73708 / 16.0)).toInt()
        rem -= aana * (508.73708 / 16.0)
        val paisa = (rem / (508.73708 / 64.0)).toInt()
        rem -= paisa * (508.73708 / 64.0)
        val dam = rem / (508.73708 / 256.0)
        return LegacyRopaniBreakdown(ropani, aana, paisa, dam)
    }

    fun breakdownToSqm(ropani: Int, aana: Int, paisa: Int, dam: Double): Double =
        ropani * 508.73708 + aana * (508.73708 / 16.0) + paisa * (508.73708 / 64.0) + dam * (508.73708 / 256.0)

    fun formatBighaBreakdown(totalSqm: Double): String {
        var rem = totalSqm
        val bigha = (rem / 6772.6316).toInt()
        rem -= bigha * 6772.6316
        val kattha = (rem / 338.63158).toInt()
        rem -= kattha * 338.63158
        val dhur = rem / 16.93158
        return String.format("%d-%d-%.2f", bigha, kattha, dhur)
    }
}
