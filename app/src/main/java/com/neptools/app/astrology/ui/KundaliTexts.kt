package com.neptools.app.astrology.ui

import com.neptools.app.astrology.data.NatalChart
import com.neptools.app.astrology.data.Planet
import com.neptools.app.astrology.data.PlanetPosition
import com.neptools.app.astrology.vedic.NakshatraCalc
import com.neptools.app.astrology.vedic.Signs
import com.neptools.app.ui.components.PlanetStyle
import com.neptools.app.ui.theme.ThemePrefs

object KundaliTexts {

    private val en get() = ThemePrefs.lang.value == "en"

    fun planetName(p: Planet): String =
        if (en) PlanetStyle.nameEn(p) else PlanetStyle.nameNp(p)

    fun role(p: Planet): String = when (p) {
        Planet.SUN -> if (en) "Confidence, vitality & leadership" else "आत्मविश्वास, ऊर्जा र नेतृत्व क्षमता"
        Planet.MOON -> if (en) "Mind, emotions & mental peace" else "मन, भावना र आन्तरिक शान्ति"
        Planet.MARS -> if (en) "Courage, ambition & physical energy" else "साहस, आँट र कार्यक्षमता"
        Planet.MERCURY -> if (en) "Intellect, speech & business skills" else "बुद्धि, सञ्चार र व्यावसायिक सीप"
        Planet.JUPITER -> if (en) "Wisdom, fortune & good decision-making" else "ज्ञान, भाग्य र सही निर्णय क्षमता"
        Planet.VENUS -> if (en) "Love, creativity, harmony & comfort" else "प्रेम, सौन्दर्य, सम्बन्ध र सुख–सुविधा"
        Planet.SATURN -> if (en) "Hard work, patience & long-term discipline" else "मेहनत, धैर्य र अनुशासन"
        Planet.RAHU -> if (en) "New ambitions, innovative drive & curiosity" else "नयाँ सोच, महत्वाकांक्षा र फराकिलो सपना"
        Planet.KETU -> if (en) "Inner insight, intuition & detachment" else "आन्तरिक ज्ञान, अन्तर्दृष्टि र आध्यात्म"
    }

    fun houseTheme(house: Int): String = when (house) {
        1 -> if (en) "Personality, physical health & self-identity" else "व्यक्तित्व, स्वास्थ्य र आत्म-पहिचान"
        2 -> if (en) "Wealth, savings, family & speech" else "धन, बचत, परिवार र बोली"
        3 -> if (en) "Initiative, courage & communications" else "पहल, आँट, भाइबहिनी र सञ्चार"
        4 -> if (en) "Home, family happiness & inner peace" else "घर, पारिवारिक सुख र मनको शान्ति"
        5 -> if (en) "Intelligence, creativity & learning" else "बुद्धि, सृजनशीलता, सन्तान र नयाँ सीप"
        6 -> if (en) "Daily work, health care & overcoming obstacles" else "दैनिक काम, स्वास्थ्य सावधानी र प्रतिस्पर्धा"
        7 -> if (en) "Marriage, relationships & partnerships" else "विवाह, जीवनसाथी र साझेदारी"
        8 -> if (en) "Transformation, depth & life's deeper lessons" else "गहिरो अध्ययन, अनुसन्धान र रूपान्तरण"
        9 -> if (en) "Higher knowledge, good fortune & mentorship" else "उच्च शिक्षा, भाग्य र अग्रजको मार्गदर्शन"
        10 -> if (en) "Career, achievements & social standing" else "करियर, सफलता र समाजमा प्रतिष्ठा"
        11 -> if (en) "Income gains, aspirations & network of friends" else "आम्दानी, इच्छापूर्ति र साथीभाइको सहयोग"
        else -> if (en) "Foreign travel, personal retreat & spirituality" else "विदेश यात्रा, लगानी र आध्यात्मिक चिन्तन"
    }

    fun signNature(signIndex: Int): String = when (signIndex) {
        0 -> if (en) "Energetic, proactive & courageous" else "जोशिलो, अघि सरेर काम गर्ने र साहसी"
        1 -> if (en) "Calm, practical, reliable & steady" else "शान्त, व्यावहारिक, भरपर्दो र स्थिर"
        2 -> if (en) "Curious, communicative & sharp-witted" else "जिज्ञासु, कुराकानीमा कुशल र तीक्ष्ण बुद्धि"
        3 -> if (en) "Caring, deeply emotional & protective" else "मायालु, भावनात्मक र परिवारप्रति समर्पित"
        4 -> if (en) "Natural leader, warm-hearted & confident" else "नेतृत्वदायी, आत्मविश्वासी र उदार मन"
        5 -> if (en) "Organized, analytical & detail-oriented" else "व्यवस्थित, विश्लेषक र काममा स्पष्ट"
        6 -> if (en) "Balanced, friendly, fair & cooperative" else "सन्तुलित, मिलनसार, न्यायप्रिय र समझदार"
        7 -> if (en) "Intuitive, passionate & deeply focused" else "अन्तर्मुखी, गम्भीर र लगनशील"
        8 -> if (en) "Optimistic, truth-seeking & enthusiastic" else "आशावादी, नयाँ कुरा सिक्न रुचाउने र स्वतन्त्र"
        9 -> if (en) "Diligent, structured & highly patient" else "मेहनती, अनुशासित र लामो धैर्य राख्ने"
        10 -> if (en) "Visionary, open-minded & helpful to all" else "दूरदर्शी, खुला सोच भएको र परोपकारी"
        else -> if (en) "Empathetic, artistic & naturally kind" else "दयालु, कल्पनाशील, संवेदनशील र कोमल मन"
    }

    fun moonSignMeaning(signIndex: Int): String =
        if (en) "Moon sign shows your emotional temperament: ${signNature(signIndex)}."
        else "चन्द्र राशिले तपाईंको भित्री मन र भावना देखाउँछ: ${signNature(signIndex)}।"

    fun lagnaMeaning(chart: NatalChart): String {
        val lord = chart.houses.first { it.index == 1 }.lord
        val lordName = planetName(lord)
        return if (en)
            "Ascendant ${Signs.en[chart.lagnaSign]} — suggests a ${signNature(chart.lagnaSign)} outlook. Guide planet: $lordName."
        else
            "लग्न ${Signs.np[chart.lagnaSign]} — ${signNature(chart.lagnaSign)} स्वभावको संकेत। लग्न-स्वामी: $lordName।"
    }

    fun nakshatraNote(name: String): String =
        if (en) "Birth Star (Nakshatra) $name reflects your core instincts and life path."
        else "जन्म नक्षत्र $name ले तपाईंको मूल स्वभाव र जीवनशैलीलाई संकेत गर्दछ।"

    fun planetLine(p: Planet, pos: PlanetPosition): String {
        val h = pos.houseFromLagna
        val pName = planetName(p)
        val retro = if (pos.retrograde) (if (en) " (Retrograde)" else " (वक्री — गहिरो प्रभाव)") else ""
        return if (en)
            "$pName$retro is placed in House $h (${houseTheme(h)}), strengthening ${role(p)}."
        else "$pName$retro घर-${h} (${houseTheme(h)}) मा बसेको छ — यसले ${role(p)} मा विशेष भूमिका खेल्छ।"
    }

    fun nakshatraOf(pos: PlanetPosition): String =
        if (en)
            NakshatraCalc.namesEn[NakshatraCalc.index(pos.siderealLon)]
        else NakshatraCalc.names[NakshatraCalc.index(pos.siderealLon)]

    fun signName(index: Int): String =
        if (en) Signs.en[index] else Signs.np[index]

    fun chartShortCode(p: Planet): String =
        if (en) p.name.take(2) else when (p) {
            Planet.SUN -> "सू"; Planet.MOON -> "चं"; Planet.MARS -> "मं"
            Planet.MERCURY -> "बु"; Planet.JUPITER -> "गु"; Planet.VENUS -> "शु"
            Planet.SATURN -> "श"; Planet.RAHU -> "रा"; Planet.KETU -> "के"
        }
}
