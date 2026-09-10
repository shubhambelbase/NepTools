package com.neptools.app.core.subscription

enum class BillingCycle(val labelNp: String, val labelEn: String, val monthsMultiplier: Double) {
    WEEKLY("साप्ताहिक", "Weekly", 0.23),
    MONTHLY("मासिक", "Monthly", 1.0),
    QUARTERLY("त्रैमासिक", "Quarterly", 3.0),
    HALF_YEARLY("अर्धवार्षिक", "Semi-Annual", 6.0),
    YEARLY("वार्षिक", "Yearly", 12.0)
}

enum class SubscriptionCategory(
    val labelNp: String,
    val labelEn: String,
    val iconKey: String,
    val colorHex: Long
) {
    STREAMING("स्ट्रिमिङ तथा मनोरञ्जन", "Streaming & Media", "play", 0xFFE11D48),
    INTERNET_MOBILE("इन्टरनेट तथा टेलिकम", "Internet & Mobile", "wifi", 0xFF0284C7),
    SOFTWARE_AI("सफ्टवेयर तथा AI", "Software & AI", "doc", 0xFF7C3AED),
    UTILITIES("बिजुली, पानी र महसुल", "Utilities & Bills", "zap", 0xFFEA580C),
    FITNESS_HEALTH("स्वास्थ्य तथा जिम", "Fitness & Health", "award", 0xFF16A34A),
    HOUSING_RENT("घरभाडा तथा आवास", "Housing & Rent", "home", 0xFFD97706),
    EDUCATION_WORK("शिक्षा तथा अध्ययन", "Education & Work", "doc", 0xFF4F46E5),
    OTHER("अन्य सेवाहरू", "Other Services", "card", 0xFF64748B)
}

enum class CurrencyType(val symbol: String, val code: String, val defaultToNprRate: Double) {
    NPR("रू", "NPR", 1.0),
    USD("$", "USD", 136.0),
    INR("₹", "INR", 1.6),
    EUR("€", "EUR", 147.0),
    GBP("£", "GBP", 172.0)
}

enum class PaymentMethod(val labelNp: String, val labelEn: String) {
    ESEWA("इ-सेवा", "eSewa"),
    KHALTI("खल्ती", "Khalti"),
    DOLLAR_CARD("डलर / भिसा कार्ड", "Dollar / Visa Card"),
    BANK_TRANSFER("बैंक / ConnectIPS", "Bank / ConnectIPS"),
    CASH("नगद", "Cash"),
    OTHER("अन्य", "Other")
}

@androidx.compose.runtime.Immutable
data class Subscription(
    val id: String,
    val nameNp: String,
    val nameEn: String,
    val price: Double,
    val currency: CurrencyType = CurrencyType.NPR,
    val billingCycle: BillingCycle = BillingCycle.MONTHLY,
    val category: SubscriptionCategory = SubscriptionCategory.OTHER,
    val paymentMethod: PaymentMethod = PaymentMethod.DOLLAR_CARD,
    val firstBillingDateIso: String, // "YYYY-MM-DD"
    val nextBillingDateIso: String,  // "YYYY-MM-DD"
    val colorHex: Long = 0xFF4F46E5,
    val notes: String = "",
    val isPaused: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * Normalized monthly cost in NPR for analytics.
     */
    fun monthlyCostInNpr(): Double {
        val inNpr = price * currency.defaultToNprRate
        return when (billingCycle) {
            BillingCycle.WEEKLY -> inNpr * 4.33
            BillingCycle.MONTHLY -> inNpr
            BillingCycle.QUARTERLY -> inNpr / 3.0
            BillingCycle.HALF_YEARLY -> inNpr / 6.0
            BillingCycle.YEARLY -> inNpr / 12.0
        }
    }

    /**
     * Normalized yearly cost in NPR for analytics.
     */
    fun yearlyCostInNpr(): Double {
        return monthlyCostInNpr() * 12.0
    }
}

data class SubscriptionSummary(
    val totalMonthlyNpr: Double,
    val totalYearlyNpr: Double,
    val activeCount: Int,
    val pausedCount: Int,
    val dueIn7DaysCount: Int,
    val upcomingRenewals: List<Subscription>,
    val categoryBreakdown: Map<SubscriptionCategory, Double>
)
