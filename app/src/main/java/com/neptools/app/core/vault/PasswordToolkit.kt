package com.neptools.app.core.vault

import java.security.SecureRandom
import kotlin.math.ln

object PasswordToolkit {

    private val random = SecureRandom()

    private const val LOWER = "abcdefghijklmnopqrstuvwxyz"
    private const val UPPER = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"
    private const val SYMBOLS = "!@#\$%^&*()-_=+[]{};:,.<>?"
    private const val AMBIGUOUS = "Il1O0oB8S5Z2G6q9"

    private val COMMON = listOf(
        "password", "passw0rd", "123456", "12345678", "qwerty", "abc123",
        "iloveyou", "admin", "welcome", "monkey", "letmein", "dragon",
        "nepal", "kathmandu", "gautam", "budha", "everest"
    )

    data class StrengthResult(val score: Int, val label: String, val entropyBits: Int)

    fun generate(
        length: Int,
        upper: Boolean,
        lower: Boolean,
        digits: Boolean,
        symbols: Boolean,
        excludeAmbiguous: Boolean
    ): String {
        fun pool(src: String): String =
            if (excludeAmbiguous) src.filterNot { it in AMBIGUOUS } else src

        val classes = ArrayList<String>()
        if (lower) classes.add(pool(LOWER))
        if (upper) classes.add(pool(UPPER))
        if (digits) classes.add(pool(DIGITS))
        if (symbols) classes.add(pool(SYMBOLS))
        if (classes.isEmpty()) classes.add(pool(LOWER))

        val all = classes.joinToString("")
        val len = length.coerceIn(4, 64)
        val chars = CharArray(len)

        for (i in classes.indices) {
            chars[i] = classes[i][random.nextInt(classes[i].length)]
        }
        for (i in classes.size until len) {
            chars[i] = all[random.nextInt(all.length)]
        }
        for (i in len - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val t = chars[i]
            chars[i] = chars[j]
            chars[j] = t
        }
        return String(chars)
    }

    fun strength(password: String): StrengthResult {
        val p = password
        if (p.isEmpty()) return StrengthResult(0, "", 0)

        val lowerPool = 26.0
        val upperPool = 26.0
        val digitPool = 10.0
        val symbolPool = 33.0
        var poolSize = 0.0
        var classCount = 0
        if (p.any { it.isLowerCase() }) { poolSize += lowerPool; classCount++ }
        if (p.any { it.isUpperCase() }) { poolSize += upperPool; classCount++ }
        if (p.any { it.isDigit() }) { poolSize += digitPool; classCount++ }
        if (p.any { !it.isLetterOrDigit() }) { poolSize += symbolPool; classCount++ }
        if (classCount == 0) poolSize = 10.0

        var entropy = (p.length * (ln(poolSize) / ln(2.0))).toInt()

        val lowered = p.lowercase()
        val isCommon = COMMON.any { lowered.contains(it) }
        if (isCommon) entropy -= 30

        var repeats = 1
        var maxRepeats = 1
        for (i in 1 until p.length) {
            if (p[i] == p[i - 1]) repeats++ else {
                maxRepeats = maxOf(maxRepeats, repeats); repeats = 1
            }
        }
        maxRepeats = maxOf(maxRepeats, repeats)
        if (maxRepeats >= 3) entropy -= (maxRepeats - 2) * 4

        var sequences = 0
        for (i in 2 until p.length) {
            val a = p[i - 2].lowercaseChar()
            val b = p[i - 1].lowercaseChar()
            val c = p[i].lowercaseChar()
            if ((c.code == b.code + 1 && b.code == a.code + 1) ||
                (c.code == b.code - 1 && b.code == a.code - 1)
            ) sequences++
        }
        entropy -= sequences * 3

        val score = when {
            p.length < 5 || isCommon && p.length < 10 -> 1
            entropy < 35 -> 1
            entropy < 55 -> 2
            entropy < 80 -> 3
            else -> 4
        }.coerceIn(if (p.isEmpty()) 0 else 1, 4)

        return StrengthResult(score, scoreLabel(score), entropy.coerceIn(0, 999))
    }

    fun scoreLabel(score: Int): String = when (score) {
        1 -> "Very Weak"
        2 -> "Weak"
        3 -> "Good"
        4 -> "Strong"
        else -> ""
    }
}
