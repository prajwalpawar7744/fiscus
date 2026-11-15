package com.prajwalpawar.fiscus.ui.components

import androidx.compose.ui.text.AnnotatedString
import org.junit.Assert.assertEquals
import org.junit.Test

class CurrencyPrefixTransformationTest {

    private val rupee = CurrencyPrefixTransformation("₹")

    @Test
    fun `prefixes the symbol for display only`() {
        assertEquals("₹1250", rupee.filter(AnnotatedString("1250")).text.text)
    }

    @Test
    fun `adds nothing to an empty field, so the placeholder can show`() {
        assertEquals("", rupee.filter(AnnotatedString("")).text.text)
    }

    @Test
    fun `cursor offsets map both ways and never leave the original text`() {
        val mapping = rupee.filter(AnnotatedString("12.5")).offsetMapping
        assertEquals(1, mapping.originalToTransformed(0))
        assertEquals(5, mapping.originalToTransformed(4))
        assertEquals(0, mapping.transformedToOriginal(0))
        assertEquals(0, mapping.transformedToOriginal(1))
        assertEquals(4, mapping.transformedToOriginal(5))
    }

    @Test
    fun `transformations with the same symbol are equal, so fields don't recompose needlessly`() {
        assertEquals(CurrencyPrefixTransformation("₹"), rupee)
        assertEquals(CurrencyPrefixTransformation("₹").hashCode(), rupee.hashCode())
    }
}
