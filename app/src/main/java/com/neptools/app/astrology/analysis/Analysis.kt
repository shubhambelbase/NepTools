package com.neptools.app.astrology.analysis

import com.neptools.app.astrology.data.CategoryScore
import com.neptools.app.astrology.data.DashaPeriod
import com.neptools.app.astrology.data.MonthRating
import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.ScoreFactor
import com.neptools.app.astrology.data.TransitInfo
import com.neptools.app.astrology.data.YogaFinding
import com.neptools.app.astrology.vedic.DashaCalculator
import com.neptools.app.astrology.vedic.HouseCalc
import com.neptools.app.astrology.vedic.StrengthCalculator
import java.time.LocalDate

data class AnalysisBundle(
    val scores: List<CategoryScore>,
    val todayLines: List<String>
)

object CategoryDefs {
    data class Def(
        val key: String,
        val labelNp: String,
        val primaryHouse: Int,
        val supportingHouses: List<Int>,
        val keyPlanets: List<Planet>,
        val dashaLordBonus: Boolean
    )

    val all = listOf(
        Def("overall", "समग्र", 1, listOf(1, 5, 9, 10), listOf(Planet.SUN, Planet.JUPITER), true),
        Def("luck", "भाग्य", 9, listOf(5, 9, 1), listOf(Planet.JUPITER), true),
        Def("career", "करियर", 10, listOf(6, 10, 11), listOf(Planet.SUN, Planet.SATURN, Planet.MARS), true),
        Def("finance", "धन", 2, listOf(2, 11), listOf(Planet.VENUS, Planet.JUPITER), true),
        Def("education", "शिक्षा", 4, listOf(2, 4, 5), listOf(Planet.MERCURY, Planet.JUPITER), true),
        Def("love", "प्रेम", 5, listOf(5, 7), listOf(Planet.VENUS, Planet.MOON), false),
        Def("marriage", "विवाह", 7, listOf(7, 2), listOf(Planet.VENUS, Planet.JUPITER), true),
        Def("family", "परिवार", 4, listOf(2, 4), listOf(Planet.MOON), false),
        Def("health", "स्वास्थ्य", 1, listOf(1, 6, 8), listOf(Planet.SATURN, Planet.MARS), false),
        Def("travel", "यात्रा", 3, listOf(3, 9, 12), listOf(Planet.MOON), false),
        Def("foreign", "विदेश", 12, listOf(12, 9), listOf(Planet.RAHU, Planet.SATURN), false),
        Def("property", "सम्पत्ति", 4, listOf(4, 11), listOf(Planet.MARS, Planet.SATURN), true),
        Def("children", "सन्तान", 5, listOf(5, 9), listOf(Planet.JUPITER), false),
        Def("reputation", "साख", 10, listOf(1, 10, 11), listOf(Planet.SUN, Planet.JUPITER), false),
        Def("growth", "आत्म-विकास", 1, listOf(1, 9, 12), listOf(Planet.KETU, Planet.JUPITER), false)
    )
}

class AnalysisBundleBuilder private constructor() {

    companion object {

        fun planetNp(p: Planet): String = when (p) {
            Planet.SUN -> "सूर्य"
            Planet.MOON -> "चन्द्र"
            Planet.MARS -> "मंगल"
            Planet.MERCURY -> "बुध"
            Planet.JUPITER -> "गुरु"
            Planet.VENUS -> "शुक्र"
            Planet.SATURN -> "शनि"
            Planet.RAHU -> "राहु"
            Planet.KETU -> "केतु"
        }

        fun build(
            chart: NatalChart,
            strengths: Map<Planet, StrengthCalculator.StrengthResult>,
            maha: DashaPeriod?,
            antar: DashaPeriod?,
            praty: DashaPeriod?,
            transits: List<TransitInfo>,
            conj: Map<Planet, Planet>,
            yogas: List<YogaFinding>
        ): AnalysisBundle {

            fun strengthOf(p: Planet) = (strengths[p]?.score ?: 50)

            fun houseOccupants(house: Int): List<Planet> =
                Planet.NINE.filter { chart.positions.getValue(it).houseFromLagna == house }

            fun houseAspects(house: Int): List<Pair<Planet, Int>> {
                val out = ArrayList<Pair<Planet, Int>>()
                Planet.SEVEN.forEach { src ->
                    val srcHouse = chart.positions.getValue(src).houseFromLagna
                    val aspects = when (src) {
                        Planet.SUN, Planet.MOON -> listOf(7)
                        Planet.MARS -> listOf(4, 7, 8)
                        Planet.JUPITER -> listOf(5, 7, 9)
                        Planet.SATURN -> listOf(3, 7, 10)
                        else -> listOf(7)
                    }
                    aspects.forEach { a ->
                        if (((srcHouse + a - 2) % 12) + 1 == house) out.add(src to a)
                    }
                }
                return out
            }

            fun transitOnHouse(house: Int): List<TransitInfo> =
                transits.filter { t ->
                    val fromLagna = HouseCalc.houseOf(t.signIndex, chart.lagnaSign)
                    fromLagna == house
                }

            val scores = CategoryDefs.all.map { def ->
                val pos = ArrayList<ScoreFactor>()
                val neg = ArrayList<ScoreFactor>()
                var raw = 55

                val lord = chart.houses.first { it.index == def.primaryHouse }.lord
                val lordStr = strengthOf(lord)
                raw += (lordStr - 50) / 3
                if (lordStr >= 62) {
                    pos.add(ScoreFactor("${def.labelNp}मा राम्रो ऊर्जा — ${planetNp(lord)}को सकारात्मक प्रभाव", true, (lordStr - 50)))
                } else if (lordStr <= 42) {
                    neg.add(ScoreFactor("${def.labelNp}मा सजगता — ${planetNp(lord)}को प्रभावले धैर्य आवश्यक", false, 50 - lordStr))
                }

                val occupants = houseOccupants(def.primaryHouse)
                occupants.forEach { p ->
                    val s = strengthOf(p)
                    val benefic = p in setOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
                    if (benefic && s > 40) {
                        raw += 7
                        pos.add(ScoreFactor("${planetNp(p)}को शुभ स्थिति — सहज वातावरण र नयाँ अवसर", true, 7))
                    } else if (!benefic && p != Planet.SUN) {
                        raw -= 5
                        neg.add(ScoreFactor("${planetNp(p)}को प्रभाव — हतार नगरी ध्यानपूर्वक अघि बढ्नुहोस्", false, 5))
                    }
                }

                houseAspects(def.primaryHouse).forEach { (p, _) ->
                    val benefic = p in setOf(Planet.JUPITER, Planet.VENUS, Planet.MERCURY, Planet.MOON)
                    if (benefic) {
                        raw += 3
                        pos.add(ScoreFactor("${planetNp(p)}को शुभ दृष्टि — सहयोग र सकारात्मक सोच", true, 3))
                    } else if (p in setOf(Planet.SATURN, Planet.MARS)) {
                        raw -= 3
                        neg.add(ScoreFactor("${planetNp(p)}को प्रभाव — संयम र स्पष्ट संवाद राख्नुहोस्", false, 3))
                    }
                }

                def.keyPlanets.forEach { kp ->
                    val s = strengthOf(kp)
                    if (s >= 60) raw += 4
                    if (s <= 40) raw -= 3
                    val involved = chart.positions.getValue(kp).houseFromLagna in
                        (listOf(def.primaryHouse) + def.supportingHouses)
                    if (involved && s >= 55) {
                        raw += 5
                        pos.add(ScoreFactor("${planetNp(kp)} अनुकूल — कार्यमा प्रगति र सन्तुष्टि", true, 5))
                    }
                }

                def.supportingHouses.forEach { sh ->
                    val shLord = chart.houses.first { it.index == sh }.lord
                    raw += (strengthOf(shLord) - 50) / 8
                }

                if (def.dashaLordBonus) {
                    val mahaHouse = maha?.let { chart.positions.getValue(it.lord).houseFromLagna }
                    val supports = mahaHouse != null &&
                        (mahaHouse == def.primaryHouse || mahaHouse in def.supportingHouses ||
                            chart.houses.first { it.index == def.primaryHouse }.lord == maha.lord)
                    if (supports) {
                        raw += 8
                        pos.add(ScoreFactor("चालु ${planetNp(maha!!.lord)} महादशाको विशेष साथ र अनुकूलता", true, 8))
                    } else if (maha != null) {
                        raw -= 3
                        neg.add(ScoreFactor("चालु समय — दैनिक प्राथमिकतामा स्पष्ट रहनुहोस्", false, 3))
                    }
                }

                transitOnHouse(def.primaryHouse).forEach { t ->
                    if (t.favorable) {
                        raw += 4
                        pos.add(ScoreFactor("वर्तमान गोचर: ${planetNp(t.planet)} अनुकूल दिशामा", true, 4))
                    } else if (t.planet in setOf(Planet.SATURN, Planet.MARS, Planet.RAHU)) {
                        raw -= 4
                        neg.add(ScoreFactor("वर्तमान गोचर: ${planetNp(t.planet)} — स्वास्थ्य र खर्चमा सतर्कता", false, 4))
                    }
                }

                yogas.forEach { y ->
                    val boost = mapOf(
                        "Gajakesari" to 6, "Budhaditya" to 4, "Chandra-Mangala" to 4,
                        "Ruchaka" to 7, "Bhadra" to 7, "Hamsa" to 7, "Malavya" to 7,
                        "Shasha" to 7, "Neecha Bhanga" to 5, "Kala Sarpa" to -6, "Kemadruma" to 3
                    )
                    boost[y.nameEn]?.let { b ->
                        if (b > 0) {
                            raw += b
                            pos.add(ScoreFactor("${y.name} योग सक्रिय — ${y.detail}", true, b))
                        } else {
                            raw += b
                            neg.add(ScoreFactor("${y.name} योग — सावधानी र धैर्य राख्नुहोस्", false, -b))
                        }
                    }
                }

                val score = raw.coerceIn(18, 96)
                CategoryScore(def.key, def.labelNp, score, pos.sortedByDescending { it.weight }, neg.sortedByDescending { it.weight })
            }

            val todayLines = buildToday(scores, maha, antar, transits, conj)
            return AnalysisBundle(scores, todayLines)
        }

        private fun buildToday(
            scores: List<CategoryScore>,
            maha: DashaPeriod?,
            antar: DashaPeriod?,
            transits: List<TransitInfo>,
            conj: Map<Planet, Planet>
        ): List<String> {
            val out = ArrayList<String>()
            val strong = scores.filter { it.score >= 62 }.sortedByDescending { it.score }.take(2)
            val weak = scores.filter { it.score <= 48 }.sortedBy { it.score }.take(2)

            if (maha != null && antar != null) {
                out.add("चालु समय: ${planetNp(maha.lord)} महादशा र ${planetNp(antar.lord)} अन्तर्दशा — ज्ञान र कार्यक्षमता बढाउने समय।")
            }
            if (strong.isNotEmpty()) {
                val strongAreas = strong.joinToString(", ") { "${it.labelNp} (${it.score}%)" }
                out.add("उत्कृष्ट क्षेत्र: $strongAreas — नयाँ काम अघि बढाउन अनुकूल।")
            }
            if (weak.isNotEmpty()) {
                val weakAreas = weak.joinToString(", ") { "${it.labelNp} (${it.score}%)" }
                out.add("ध्यान दिनुपर्ने: $weakAreas — हतार नगरी सन्तुलन कायम राख्नुहोस्।")
            }
            val saturn = transits.firstOrNull { it.planet == Planet.SATURN }
            if (saturn != null) {
                out.add(if (saturn.favorable) "शनि गोचर अनुकूल: निरन्तर मेहनत र अनुशासनले राम्रो प्रतिफल मिल्नेछ।"
                        else "शनि गोचर: काममा धैर्य राख्नुहोस्, स्वास्थ्य र समय व्यवस्थापनमा ध्यान दिनुहोस्।")
            }
            return out
        }
    }
}

object TimelineBuilder {

    fun buildMonthly(
        chart: NatalChart,
        strengths: Map<Planet, StrengthCalculator.StrengthResult>,
        calc: DashaCalculator,
        roots: List<DashaPeriod>
    ): List<MonthRating> {
        val out = ArrayList<MonthRating>()
        var cursor = LocalDate.now().withDayOfMonth(1)
        for (i in 0 until 12) {
            val monthStart = cursor.plusMonths(i.toLong())
            val (maha, antar, _) = calc.current(monthStart.plusDays(14), roots)
            val ratings = LinkedHashMap<String, String>()
            listOf("career" to "करियर", "finance" to "धन", "love" to "प्रेम", "education" to "शिक्षा").forEach { (key, np) ->
                val def = CategoryDefs.all.first { it.key == key }
                val lord = chart.houses.first { it.index == def.primaryHouse }.lord
                val base = (strengths[lord]?.score ?: 50)
                val dashaBoost = when {
                    maha?.lord == lord || antar?.lord == lord -> 12
                    maha?.lord in def.keyPlanets || antar?.lord in def.keyPlanets -> 6
                    else -> 0
                }
                val jupSign = jupiterSignApprox(monthStart.plusDays(14))
                val jupHouse = HouseCalc.houseOf(jupSign, chart.lagnaSign)
                val jupBoost = if (jupHouse in setOf(1, 5, 9, 11) || jupHouse == def.primaryHouse) 6 else 0
                val score = (base / 2 + 22 + dashaBoost + jupBoost).coerceIn(20, 95)
                val label = when {
                    score >= 68 -> "Strong"
                    score >= 50 -> "Good"
                    else -> "Moderate"
                }
                ratings[key] = "$np · $label"
            }
            out.add(MonthRating(monthStart, ratings))
        }
        return out
    }

    private fun jupiterSignApprox(date: LocalDate): Int {
        val daysSince2000 = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.of(2000, 1, 1), date)
        val siderealPeriodDays = 4332.589
        val lon = ((daysSince2000 / siderealPeriodDays) * 360.0 + 34.4) % 360.0
        val posLon = if (lon < 0) lon + 360.0 else lon
        return (posLon / 30.0).toInt() % 12
    }
}
