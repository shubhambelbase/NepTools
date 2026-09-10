package com.neptools.app.core.data

import com.neptools.app.core.calendar.BsCalendarEngine
import com.neptools.app.core.calendar.Festival

object FestivalValidator {
    data class ValidationResult(
        val valid: Map<String, Map<Int, List<Festival>>>,
        val droppedOutOfRange: Int,
        val deduplicated: Int,
        val warnings: List<String>
    )

    fun validate(
        raw: Map<String, Map<Int, List<Festival>>>,
        engine: BsCalendarEngine
    ): ValidationResult {
        val out = HashMap<String, Map<Int, List<Festival>>>()
        val warnings = mutableListOf<String>()
        var dropped = 0
        var deduped = 0

        for ((key, byDay) in raw) {
            val parts = key.split("-")
            if (parts.size != 2) {
                warnings.add("Invalid key format: $key")
                continue
            }
            val year = parts[0].toIntOrNull()
            val month = parts[1].toIntOrNull()
            if (year == null || month == null || year !in engine.supportedRange() || month !in 1..12) {
                warnings.add("Out-of-range month key: $key")
                continue
            }
            val maxDay = try { engine.monthLength(year, month) } catch (e: Exception) { 0 }
            val filtered = HashMap<Int, MutableList<Festival>>()
            for ((day, list) in byDay) {
                if (day < 1 || day > maxDay) {
                    dropped += list.size
                    warnings.add("Dropped $key day $day (max $maxDay)")
                    continue
                }
                val seen = HashSet<String>()
                for (f in list) {
                    val dedupKey = "${f.nameNp}|${f.nameEn}|${f.isPublicHoliday}"
                    if (!seen.add(dedupKey)) {
                        deduped++
                        continue
                    }
                    filtered.getOrPut(day) { mutableListOf() }.add(f)
                }
            }
            if (filtered.isNotEmpty()) out[key] = filtered
        }
        return ValidationResult(out, dropped, deduped, warnings)
    }

}
