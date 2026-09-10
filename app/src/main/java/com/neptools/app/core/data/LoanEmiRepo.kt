package com.neptools.app.core.data

import kotlin.math.pow

data class EmiResult(
    val monthlyEmi: Double,
    val totalPrincipal: Double,
    val totalInterest: Double,
    val totalPayment: Double,
    val principalPercent: Float,
    val interestPercent: Float
)

data class FdResult(
    val principal: Double,
    val grossInterest: Double,
    val taxDeduction: Double, // 6% TDS
    val netInterest: Double,
    val maturityAmount: Double
)

object LoanEmiRepo {

    fun calculateEmi(principal: Double, annualRate: Double, tenureMonths: Int): EmiResult {
        if (principal <= 0 || tenureMonths <= 0) {
            return EmiResult(0.0, 0.0, 0.0, 0.0, 100f, 0f)
        }
        if (annualRate <= 0) {
            val emi = principal / tenureMonths
            return EmiResult(emi, principal, 0.0, principal, 100f, 0f)
        }

        val monthlyRate = (annualRate / 12.0) / 100.0
        val factor = (1.0 + monthlyRate).pow(tenureMonths.toDouble())
        val emi = (principal * monthlyRate * factor) / (factor - 1.0)
        val totalPayment = emi * tenureMonths
        val totalInterest = totalPayment - principal

        val principalPercent = ((principal / totalPayment) * 100.0).toFloat().coerceIn(0f, 100f)
        val interestPercent = 100f - principalPercent

        return EmiResult(
            monthlyEmi = emi,
            totalPrincipal = principal,
            totalInterest = totalInterest,
            totalPayment = totalPayment,
            principalPercent = principalPercent,
            interestPercent = interestPercent
        )
    }

    fun calculateFd(
        principal: Double,
        annualRate: Double,
        tenureMonths: Int,
        compoundingPerYear: Int = 4 // quarterly standard in Nepal
    ): FdResult {
        if (principal <= 0 || tenureMonths <= 0 || annualRate <= 0) {
            return FdResult(principal, 0.0, 0.0, 0.0, principal)
        }

        val years = tenureMonths / 12.0
        val r = (annualRate / 100.0) / compoundingPerYear
        val n = compoundingPerYear * years
        val maturityGross = principal * (1.0 + r).pow(n)
        val grossInterest = maturityGross - principal
        val taxDeduction = grossInterest * 0.06 // 6% TDS in Nepal
        val netInterest = grossInterest - taxDeduction
        val maturityAmount = principal + netInterest

        return FdResult(
            principal = principal,
            grossInterest = grossInterest,
            taxDeduction = taxDeduction,
            netInterest = netInterest,
            maturityAmount = maturityAmount
        )
    }
}
