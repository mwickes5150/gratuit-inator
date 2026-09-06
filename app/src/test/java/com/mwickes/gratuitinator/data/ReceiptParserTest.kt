package com.mwickes.gratuitinator.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ReceiptParserTest {

    @Test
    fun `standard subtotal and tax lines parse correctly`() {
        val result = ReceiptParser.parseReceipt(
            listOf("Subtotal $84.20", "Tax $7.16", "Total $91.36"),
        )
        assertEquals("84.20", result.subtotal)
        assertEquals("7.16", result.tax)
    }

    @Test
    fun `suggested tip line is never captured as subtotal or tax`() {
        val result = ReceiptParser.parseReceipt(
            listOf("Subtotal 84.20", "Suggested Tip 20% $8.43", "Tax 7.16"),
        )
        assertEquals("84.20", result.subtotal)
        assertEquals("7.16", result.tax)
    }

    @Test
    fun `gratuity line alone does not leak into either field`() {
        val result = ReceiptParser.parseReceipt(
            listOf("Gratuity 18% $15.20", "Suggested 20%"),
        )
        assertNull(result.subtotal)
        assertNull(result.tax)
    }

    @Test
    fun `derives subtotal from total minus tax when subtotal line is missing`() {
        val result = ReceiptParser.parseReceipt(
            listOf("Total $91.36", "Tax $7.16"),
        )
        assertEquals("84.20", result.subtotal)
        assertEquals("7.16", result.tax)
    }

    @Test
    fun `two-column layout with labels disconnected from amounts uses the last consistent triple`() {
        // Real-world case: a restaurant "guest check" copy prints Subtotal/Tax/Total labels with
        // no amount at all, then a separate settlement footer later prints the actual dollar
        // figures with no labels nearby. ML Kit's line order interleaves item-price math (15+18=33)
        // before the real subtotal/tax/total (33+2.64=35.64) — the LAST consistent triple must win.
        val result = ReceiptParser.parseReceipt(
            listOf(
                "Guest Count: 1",
                "Ordered:",
                "Subtotal",
                "Tax",
                "5950 Butternut Drive",
                "East Syracuse, NY 13057",
                "Total",
                "2 Prison City Mass Riot (Hazy IPA)",
                "29 ounce Canyon Road Pinot Grigio",
                "Visa",
                "8/4/26 7:52 PM",
                "$15.00",
                "$18.00",
                "Debit Card Incremental pre-authorization",
                "$33.00",
                "$2.64",
                "$35.64",
                "XXXXXXXX2848",
                "8:09 PM",
            ),
        )
        assertEquals("33.00", result.subtotal)
        assertEquals("2.64", result.tax)
    }

    @Test
    fun `garbled or empty input yields both fields null`() {
        val garbled = ReceiptParser.parseReceipt(listOf("asdkj 213", "???"))
        assertNull(garbled.subtotal)
        assertNull(garbled.tax)

        val empty = ReceiptParser.parseReceipt(emptyList())
        assertNull(empty.subtotal)
        assertNull(empty.tax)
    }
}
