package com.prajwalpawar.fiscus.ui.screens.transaction

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AmountInputTest {
    @Test
    fun acceptsPartialAndCompleteAmounts() {
        listOf("", "0", "12", "12.", "12.3", "12.34", ".5").forEach {
            assertTrue("expected '$it' to be accepted", isAmountInput(it))
        }
    }

    @Test
    fun rejectsExtraDecimalsAndNonDigits() {
        listOf("12.345", "1.2.3", "12a", "-5", "1,5", " 1").forEach {
            assertFalse("expected '$it' to be rejected", isAmountInput(it))
        }
    }
}
