package com.mwickes.gratuitinator.data

/**
 * Result of parsing OCR'd receipt text. Either field is `null` when not confidently matched —
 * the parser never guesses.
 */
data class ParsedReceipt(
    val subtotal: String?,
    val tax: String?,
)

/**
 * Pure text-parsing heuristics for a receipt's Subtotal/Tax lines. Zero Android imports —
 * testable without mocking ML Kit types, matching [com.mwickes.gratuitinator.domain.TipCalculator]'s
 * pure-Kotlin convention.
 *
 * Hard requirement: any line that looks like a tip/gratuity/suggested-tip line is excluded from
 * consideration entirely, before anything else runs — a suggested-tip line printed on a receipt
 * must never be parsed into Subtotal or Tax (or any other field).
 *
 * This is a conservative first pass — real-world tuning against varied receipt layouts is
 * expected to follow later. One such case is already handled: some receipts (e.g. restaurant
 * "guest check" copies meant for a tip line) print Subtotal/Tax/Total labels with no amount
 * attached at all, and the real dollar figures appear later, disconnected from any label, in a
 * payment-settlement section. For that layout, same-line/label matching can never work, so a
 * fallback below looks for three *consecutive* amounts `(a, b, c)` where `a + b == c` — a genuine
 * arithmetic check on real numbers, not a guess.
 */
object ReceiptParser {

    private val tipLineRegex = Regex("tip|gratuity|suggested", RegexOption.IGNORE_CASE)
    private val subtotalLineRegex = Regex("sub[\\s-]?total", RegexOption.IGNORE_CASE)
    private val taxLineRegex = Regex("tax|vat", RegexOption.IGNORE_CASE)
    private val totalLineRegex = Regex("total", RegexOption.IGNORE_CASE)
    private val currencyRegex = Regex("""\$?\s*(\d{1,4}(?:[.,]\d{2}))""")

    fun parseReceipt(lines: List<String>): ParsedReceipt {
        val candidateLines = lines.filterNot { tipLineRegex.containsMatchIn(it) }

        val subtotal = candidateLines
            .firstOrNull { subtotalLineRegex.containsMatchIn(it) }
            ?.let { extractCurrency(it) }

        val tax = candidateLines
            .firstOrNull { taxLineRegex.containsMatchIn(it) }
            ?.let { extractCurrency(it) }

        if (subtotal != null) {
            return ParsedReceipt(subtotal = subtotal, tax = tax)
        }

        // Fallback 1: derive subtotal = total - tax, if a (non-subtotal) Total line and Tax are both found.
        val total = candidateLines
            .firstOrNull { totalLineRegex.containsMatchIn(it) && !subtotalLineRegex.containsMatchIn(it) }
            ?.let { extractCurrency(it) }

        val totalAmount = total?.toBigDecimalOrNullSafe()
        val taxAmount = tax?.toBigDecimalOrNullSafe()
        if (totalAmount != null && taxAmount != null) {
            return ParsedReceipt(subtotal = formatAmount(totalAmount - taxAmount), tax = tax)
        }

        // Fallback 2: labels are disconnected from their amounts entirely (e.g. a blank
        // guest-check summary followed by a separate settlement footer). Find every amount in
        // document order and look for a consecutive triple (a, b, c) with a + b == c; take the
        // *last* one, since real subtotal/tax/total tends to appear after any incidental
        // item-price math earlier in the receipt.
        val amounts = candidateLines.mapNotNull { extractCurrency(it)?.toBigDecimalOrNullSafe() }
        val lastConsistentTriple = (0..amounts.size - 3).lastOrNull { i ->
            (amounts[i] + amounts[i + 1] - amounts[i + 2]).abs() <= CENT
        }

        return if (lastConsistentTriple != null) {
            ParsedReceipt(
                subtotal = formatAmount(amounts[lastConsistentTriple]),
                tax = formatAmount(amounts[lastConsistentTriple + 1]),
            )
        } else {
            ParsedReceipt(subtotal = null, tax = tax)
        }
    }

    private val CENT = java.math.BigDecimal("0.01")

    private fun extractCurrency(line: String): String? =
        currencyRegex.find(line)?.groupValues?.get(1)?.replace(',', '.')

    private fun String.toBigDecimalOrNullSafe(): java.math.BigDecimal? =
        try {
            java.math.BigDecimal(this)
        } catch (e: NumberFormatException) {
            null
        }

    private fun formatAmount(value: java.math.BigDecimal): String =
        value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString()
}
