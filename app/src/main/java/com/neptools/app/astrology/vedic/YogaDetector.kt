package com.neptools.app.astrology.vedic

import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.YogaFinding

class YogaDetector {

    fun detect(chart: NatalChart): List<YogaFinding> {
        val en = com.neptools.app.ui.theme.ThemePrefs.lang.value == "en"
        val out = ArrayList<YogaFinding>()
        val pos = chart.positions
        val lagnaSign = chart.lagnaSign

        val sunSign = pos.getValue(Planet.SUN).signIndex
        val moonSign = pos.getValue(Planet.MOON).signIndex

        if (pos.getValue(Planet.MERCURY).signIndex == sunSign) {
            out.add(
                YogaFinding(
                    "बुधादित्य", "Budhaditya", true,
                    "सूर्य-बुध एकै राशि (${Signs.np[sunSign]}) — बुद्धि/सञ्चार बल"
                )
            )
        }
        if (pos.getValue(Planet.MARS).signIndex == moonSign) {
            out.add(YogaFinding(if (en) "Chandra-Mangala" else "चन्द्र-मंगल", "Chandra-Mangala", true,
                    if (en) "Moon-Mars conjunction — business drive" else "चन्द्र-मंगल युति — व्यावसायिक drive"))
        }

        run {
            var jupHouseFromMoon = ((pos.getValue(Planet.JUPITER).signIndex - moonSign + 12) % 12) + 1
            if (jupHouseFromMoon in setOf(1, 4, 7, 10)) {
                out.add(
                    YogaFinding("गजकेसरी", "Gajakesari", true,
                        "चन्द्रबाट केन्द्रमा गुरु (house $jupHouseFromMoon)")
                )
            } else jupHouseFromMoon = 0
        }

        run {
            val others = Planet.SEVEN.filter { it != Planet.MOON && it != Planet.SUN }
                .all { p ->
                    val h = HouseCalc.houseFromMoon(pos.getValue(p).signIndex, moonSign)
                    h !in setOf(2, 12) && pos.getValue(p).signIndex != moonSign
                }
            if (others) {
                out.add(
                    YogaFinding(
                        name = if (en) "Kemadruma" else "केमद्रुम",
                        nameEn = "Kemadruma",
                        present = true,
                        detail = if (en) "No planets in 2nd/12th from Moon — independent journey"
                                 else "चन्द्रको अघि-पछि ग्रह नभएको — स्वावलम्बन"
                    )
                )
            }
        }

        val mahapurush = mapOf(
            Planet.MARS to "रुचक", Planet.MERCURY to "भद्र",
            Planet.JUPITER to "हंस", Planet.VENUS to "मालव्य",
            Planet.SATURN to "शश"
        )
        for ((p, name) in mahapurush) {
            val sp = pos.getValue(p)
            val own = ownOrExalted(p, sp.signIndex)
            val kendra = ((sp.signIndex - lagnaSign + 12) % 12) in listOf(0, 3, 6, 9)
            if (own && kendra) {
                out.add(
                    YogaFinding("$name योग", "Panch Mahapurush — ${nameEn(p)}", true,
                        "$p केन्द्रमा स्व/उच्च राशि")
                )
            }
        }

        run {
            val seven = Planet.SEVEN.map { pos.getValue(it).signIndex }
            val rahuSign = pos.getValue(Planet.RAHU).signIndex
            val allSideA = seven.all { ((it - rahuSign + 12) % 12) in 1..5 }
            val allSideB = seven.all { ((it - rahuSign + 12) % 12) in 7..11 }
            if (allSideA || allSideB) {
                out.add(
                    YogaFinding(if (en) "Kala Sarpa" else "काल सर्प", "Kala Sarpa", true,
                        if (en) "All planets within one side of Rahu-Ketu axis" else "सबै ग्रह राहु-केतु अक्षको एकातर्फ")
                )
            }
        }

        Planet.SEVEN.forEach { p ->
            val sp = pos.getValue(p)
            val dispositor = signLord(sp.signIndex)
            if (debilitated(p, sp.signIndex)) {
                val dHouse = HouseCalc.houseOf(pos.getValue(dispositor).signIndex, lagnaSign)
                if (dHouse in setOf(1, 4, 7, 10)) {
                    out.add(
                        YogaFinding("नीच भंग", "Neecha Bhanga", true,
                            "$p नीच तर राशि-स्वामी केन्द्रमा")
                    )
                }
            }
        }

        return out
    }

    private fun nameEn(p: Planet): String = when (p) {
        Planet.MARS -> "Ruchaka"; Planet.MERCURY -> "Bhadra"
        Planet.JUPITER -> "Hamsa"; Planet.VENUS -> "Malavya"
        else -> "Shasha"
    }

    private fun ownOrExalted(p: Planet, sign: Int): Boolean {
        val exalt = mapOf(Planet.SUN to 0, Planet.MOON to 1, Planet.MARS to 9,
            Planet.MERCURY to 5, Planet.JUPITER to 3, Planet.VENUS to 11, Planet.SATURN to 6)
        val own = mapOf(
            Planet.SUN to setOf(4), Planet.MOON to setOf(3),
            Planet.MARS to setOf(0, 7), Planet.MERCURY to setOf(2, 5),
            Planet.JUPITER to setOf(8, 11), Planet.VENUS to setOf(1, 6),
            Planet.SATURN to setOf(9, 10)
        )
        return sign == exalt[p] || own.getValue(p).contains(sign)
    }

    private fun debilitated(p: Planet, sign: Int): Boolean {
        val deb = mapOf(Planet.SUN to 6, Planet.MOON to 7, Planet.MARS to 3,
            Planet.MERCURY to 11, Planet.JUPITER to 9, Planet.VENUS to 5, Planet.SATURN to 0)
        return deb[p] == sign
    }

    private fun signLord(sign: Int): Planet = when (sign) {
        0 -> Planet.MARS; 1 -> Planet.VENUS; 2 -> Planet.MERCURY; 3 -> Planet.MOON
        4 -> Planet.SUN; 5 -> Planet.MERCURY; 6 -> Planet.VENUS; 7 -> Planet.MARS
        8 -> Planet.JUPITER; 9 -> Planet.SATURN; 10 -> Planet.SATURN; else -> Planet.JUPITER
    }

    private fun isBetweenRahuKetu(sign: Int, rahu: Int, ketu: Int): Boolean {
        val dist = (sign - rahu + 12) % 12
        return dist in 1..11 && sign != ketu
    }
}

object HouseCalc {
    fun houseOf(sign: Int, lagnaSign: Int): Int = ((sign - lagnaSign + 12) % 12) + 1
    fun houseFromMoon(sign: Int, moonSign: Int): Int = houseOf(sign, moonSign)
}
