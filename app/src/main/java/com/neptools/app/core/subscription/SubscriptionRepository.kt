package com.neptools.app.core.subscription

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import java.util.UUID

class SubscriptionRepository private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("patro_subscriptions_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_SUBS = "subscriptions_list"

        @Volatile
        private var instance: SubscriptionRepository? = null

        fun get(context: Context): SubscriptionRepository {
            return instance ?: synchronized(this) {
                instance ?: SubscriptionRepository(context.applicationContext).also { instance = it }
            }
        }
    }

    @Synchronized
    fun getAllSubscriptions(): List<Subscription> {
        val raw = prefs.getString(KEY_SUBS, null) ?: return emptyList()
        val list = mutableListOf<Subscription>()
        try {
            val jsonArr = JSONArray(raw)
            for (i in 0 until jsonArr.length()) {
                val obj = jsonArr.getJSONObject(i)
                val curType = try { CurrencyType.valueOf(obj.getString("currency")) } catch (e: Exception) { CurrencyType.NPR }
                val cycle = try { BillingCycle.valueOf(obj.getString("billingCycle")) } catch (e: Exception) { BillingCycle.MONTHLY }
                val cat = try { SubscriptionCategory.valueOf(obj.getString("category")) } catch (e: Exception) { SubscriptionCategory.OTHER }
                val pm = try { PaymentMethod.valueOf(obj.getString("paymentMethod")) } catch (e: Exception) { PaymentMethod.DOLLAR_CARD }

                val sub = Subscription(
                    id = obj.getString("id"),
                    nameNp = obj.getString("nameNp"),
                    nameEn = obj.getString("nameEn"),
                    price = obj.getDouble("price"),
                    currency = curType,
                    billingCycle = cycle,
                    category = cat,
                    paymentMethod = pm,
                    firstBillingDateIso = obj.optString("firstBillingDateIso", LocalDate.now().toString()),
                    nextBillingDateIso = obj.optString("nextBillingDateIso", LocalDate.now().plusMonths(1).toString()),
                    colorHex = obj.optLong("colorHex", 0xFF4F46E5),
                    notes = obj.optString("notes", ""),
                    isPaused = obj.optBoolean("isPaused", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis())
                )
                list.add(sub)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return list
    }

    @Synchronized
    fun saveSubscription(subscription: Subscription) {
        val list = getAllSubscriptions().toMutableList()
        val idx = list.indexOfFirst { it.id == subscription.id }
        if (idx >= 0) {
            list[idx] = subscription
        } else {
            list.add(subscription)
        }
        saveSubscriptionsList(list)
    }

    @Synchronized
    fun deleteSubscription(id: String) {
        val list = getAllSubscriptions().filter { it.id != id }
        saveSubscriptionsList(list)
    }

    @Synchronized
    fun togglePause(id: String) {
        val list = getAllSubscriptions().map {
            if (it.id == id) it.copy(isPaused = !it.isPaused) else it
        }
        saveSubscriptionsList(list)
    }

    /**
     * Advances the billing date to the next cycle when user marks as paid.
     */
    @Synchronized
    fun markAsPaidAndAdvance(id: String) {
        val list = getAllSubscriptions().map { sub ->
            if (sub.id == id) {
                val curNext = try {
                    LocalDate.parse(sub.nextBillingDateIso)
                } catch (e: Exception) {
                    LocalDate.now()
                }
                val newNext = when (sub.billingCycle) {
                    BillingCycle.WEEKLY -> curNext.plusWeeks(1)
                    BillingCycle.MONTHLY -> curNext.plusMonths(1)
                    BillingCycle.QUARTERLY -> curNext.plusMonths(3)
                    BillingCycle.HALF_YEARLY -> curNext.plusMonths(6)
                    BillingCycle.YEARLY -> curNext.plusYears(1)
                }
                sub.copy(nextBillingDateIso = newNext.toString())
            } else sub
        }
        saveSubscriptionsList(list)
    }

    private fun saveSubscriptionsList(list: List<Subscription>) {
        val jsonArr = JSONArray()
        list.forEach { sub ->
            val obj = JSONObject().apply {
                put("id", sub.id)
                put("nameNp", sub.nameNp)
                put("nameEn", sub.nameEn)
                put("price", sub.price)
                put("currency", sub.currency.name)
                put("billingCycle", sub.billingCycle.name)
                put("category", sub.category.name)
                put("paymentMethod", sub.paymentMethod.name)
                put("firstBillingDateIso", sub.firstBillingDateIso)
                put("nextBillingDateIso", sub.nextBillingDateIso)
                put("colorHex", sub.colorHex)
                put("notes", sub.notes)
                put("isPaused", sub.isPaused)
                put("createdAt", sub.createdAt)
            }
            jsonArr.put(obj)
        }
        prefs.edit().putString(KEY_SUBS, jsonArr.toString()).apply()
    }

    /**
     * Compute summary statistics and upcoming renewals.
     */
    fun getSummary(): SubscriptionSummary {
        val all = getAllSubscriptions()
        val active = all.filter { !it.isPaused }
        val paused = all.filter { it.isPaused }

        val today = LocalDate.now()
        var totalMonthly = 0.0
        val catMap = mutableMapOf<SubscriptionCategory, Double>()

        active.forEach { sub ->
            val mCost = sub.monthlyCostInNpr()
            totalMonthly += mCost
            catMap[sub.category] = (catMap[sub.category] ?: 0.0) + mCost
        }

        val totalYearly = totalMonthly * 12.0

        // Find renewals due within 7 days
        val upcoming = active.filter { sub ->
            try {
                val nextDate = LocalDate.parse(sub.nextBillingDateIso)
                val days = ChronoUnit.DAYS.between(today, nextDate)
                days in 0..7
            } catch (e: Exception) {
                false
            }
        }.sortedBy {
            try { LocalDate.parse(it.nextBillingDateIso) } catch (e: Exception) { today }
        }

        return SubscriptionSummary(
            totalMonthlyNpr = totalMonthly,
            totalYearlyNpr = totalYearly,
            activeCount = active.size,
            pausedCount = paused.size,
            dueIn7DaysCount = upcoming.size,
            upcomingRenewals = upcoming,
            categoryBreakdown = catMap
        )
    }

    fun daysUntilRenewal(sub: Subscription): Long {
        return try {
            val today = LocalDate.now()
            val nextDate = LocalDate.parse(sub.nextBillingDateIso)
            ChronoUnit.DAYS.between(today, nextDate)
        } catch (e: Exception) {
            0L
        }
    }

    fun createNewSubscription(
        nameNp: String,
        nameEn: String,
        price: Double,
        currency: CurrencyType,
        billingCycle: BillingCycle,
        category: SubscriptionCategory,
        paymentMethod: PaymentMethod,
        firstBillingDateIso: String,
        nextBillingDateIso: String,
        colorHex: Long,
        notes: String
    ): Subscription {
        val sub = Subscription(
            id = "sub_${UUID.randomUUID()}",
            nameNp = nameNp.ifBlank { nameEn },
            nameEn = nameEn.ifBlank { nameNp },
            price = price.coerceAtLeast(0.0),
            currency = currency,
            billingCycle = billingCycle,
            category = category,
            paymentMethod = paymentMethod,
            firstBillingDateIso = firstBillingDateIso,
            nextBillingDateIso = nextBillingDateIso,
            colorHex = colorHex,
            notes = notes
        )
        saveSubscription(sub)
        return sub
    }
}
